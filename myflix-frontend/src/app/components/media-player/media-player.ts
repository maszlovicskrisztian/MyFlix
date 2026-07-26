import { Component, computed, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth-service';
import { WatchProgressService } from '../../services/watch-progress-service';

@Component({
  selector: 'app-media-player',
  imports: [RouterLink],
  templateUrl: './media-player.html',
  styleUrl: './media-player.scss',
})
export class MediaPlayer implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private watchProgressService = inject(WatchProgressService);

  videoRef = viewChild<ElementRef<HTMLVideoElement>>('videoElement');
  mediaId = signal<string | null>(null);
  profileId = 1; // egyelőre hardcode, amíg nincs profil-választó UI

  private lastSentPosition = 0;

  streamUrl = computed(() => {
    const token = this.authService.getToken();
    return `${environment.apiUrl}/media/${this.mediaId()}/stream?token=${token}`;
  });

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
  }
  
  onLoadedMetadata(): void {
    const mediaId = this.mediaId();
    if (mediaId === null) return;

    this.watchProgressService.getProgress(mediaId, this.profileId).subscribe({
      next: (progress) => {
        const video = this.videoRef()?.nativeElement;
        if (video && progress.progressSeconds > 0) {
          video.currentTime = progress.progressSeconds;
        }
      },
      error: () => {}
    });
  }

  onTimeUpdate(): void {
    const video = this.videoRef()?.nativeElement;
    const mediaId = this.mediaId();
    
    if (!video || mediaId === null) 
      return;

    const currentPosition = Math.floor(video.currentTime);

    if (Math.abs(currentPosition - this.lastSentPosition) >= 10) {
      this.lastSentPosition = currentPosition;
      this.watchProgressService.updateProgress(mediaId, this.profileId, currentPosition).subscribe();
    }
  }
}
