import { AfterViewInit, Component, computed, ElementRef, inject, OnDestroy, OnInit, signal, viewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthService } from '../../services/auth-service';
import { ProfileService } from '../../services/profile-service';
import Hls from 'hls.js/dist/hls.min.js';
import { MediaService } from '../../services/media-service';

@Component({
  selector: 'app-media-player',
  imports: [],
  templateUrl: './media-player.html',
  styleUrl: './media-player.scss',
})
export class MediaPlayer implements AfterViewInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private profileService = inject(ProfileService);
  private mediaService = inject(MediaService);

  videoRef = viewChild<ElementRef<HTMLVideoElement>>('videoElement');
  mediaId = signal<number | null>(null);
  profileId = computed(() => this.profileService.selectedProfileId());
  playbackMode = signal<'DIRECT' | 'HLS' | null>(null);

  private hls: Hls | null = null;
  private hlsStartOffset = 0;
  private lastSentPosition = 0;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.mediaId.set(idParam ? Number(idParam) : null);
  }

  onTimeUpdate(): void {
    const video = this.videoRef()?.nativeElement;
    const profileId = this.profileId();
    const mediaId = this.mediaId();
    if (!video || mediaId === null || profileId === null) return;

    const offset = this.playbackMode() === 'HLS' ? this.hlsStartOffset : 0;
    const currentPosition = Math.floor(video.currentTime) + offset;

    if (Math.abs(currentPosition - this.lastSentPosition) >= 10) {
      this.lastSentPosition = currentPosition;
      this.mediaService.updateProgress(mediaId, profileId, currentPosition).subscribe();
    }
  }

  ngAfterViewInit(): void {
    const video = this.videoRef()?.nativeElement;
    const id = this.mediaId();
    if (video && id !== null) {
      this.startWithResumePoint(video, id);
    }
  }

  private startWithResumePoint(video: HTMLVideoElement, mediaId: number): void {
    const profileId = this.profileId();
    if (profileId === null) return;
    
    this.mediaService.getPlaybackInfo(mediaId, profileId).subscribe({
      next: (info) => {
        this.playbackMode.set(info.mode);
        const fullUrl = `${environment.apiUrl}${info.url}`;

        if (info.mode === 'DIRECT') {
          video.src = this.withToken(fullUrl);
          video.addEventListener('loadedmetadata', () => {
            if (info.progressSeconds > 0) video.currentTime = info.progressSeconds;
          }, { once: true });
          video.play().catch((err) => console.error('Lejátszás indítása sikertelen:', err));
        } else {
          this.hlsStartOffset = info.progressSeconds;
          this.startHls(video, fullUrl + '?startSeconds=' + info.progressSeconds);
        }
      },
      error: (err) => console.error('playback-info HIBA: ' + JSON.stringify(err)),
    });
  }


  private startHls(video: HTMLVideoElement, playlistUrl: string): void {
    if (Hls.isSupported()) {
      this.hls = new Hls({
        startPosition: 0,
        xhrSetup: (xhr) => {
          xhr.setRequestHeader('Authorization', `Bearer ${this.authService.getToken()}`);
        },
      });

      this.hls.on(Hls.Events.MANIFEST_PARSED, () => {
        video.play().catch((err) => console.error('Lejátszás indítása sikertelen:', err));
      });
      this.hls.on(Hls.Events.ERROR, (_event, data) => {
        console.error('hls.js hiba:', data.type, data.details, data.fatal);
      });

      this.hls.loadSource(playlistUrl);
      this.hls.attachMedia(video);
      return;
    }

    if (video.canPlayType('application/vnd.apple.mpegurl')) {
      video.src = this.withToken(playlistUrl);
      video.play()
        .catch((err) => console.error('Natív HLS lejátszás indítása sikertelen:', err));
      return;
    }

    console.error('A böngésző nem támogatja az HLS lejátszást.');
}

  private withToken(url: string): string {
    const token = this.authService.getToken();
    const separator = url.includes('?') ? '&' : '?';
    return `${url}${separator}token=${token}`;
  }

  ngOnDestroy(): void {
    this.hls?.destroy();
  }
}
