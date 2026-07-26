import { Component, computed, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth-service';
import { WatchProgressService } from '../../services/watch-progress-service';
import { ProfileService } from '../../services/profile-service';

@Component({
  selector: 'app-media-player',
  imports: [RouterLink],
  templateUrl: './media-player.html',
  styleUrl: './media-player.scss',
})
export class MediaPlayer implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private profileService = inject(ProfileService);
  private watchProgressService = inject(WatchProgressService);

  videoRef = viewChild<ElementRef<HTMLVideoElement>>('videoElement');
  mediaId = signal<string | null>(null);
  profileId = signal<number | null>(null);

  private lastSentPosition = 0;

  streamUrl = computed(() => {
    const token = this.authService.getToken();
    return `${environment.apiUrl}/media/${this.mediaId()}/stream?token=${token}`;
  });

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
    this.profileId.set(this.profileService.selectedProfileId());
  }
  
  onLoadedMetadata(): void {
    const mediaId = this.mediaId();
    const profileId = this.profileId();
    if (mediaId === null || profileId === null) 
      return;

    this.watchProgressService.getProgress(mediaId, profileId).subscribe({
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
    const profileId = this.profileId();
    const mediaId = this.mediaId();
    
    if (!video || mediaId === null || profileId === null) 
      return;

    const currentPosition = Math.floor(video.currentTime);

    if (Math.abs(currentPosition - this.lastSentPosition) >= 10) {
      this.lastSentPosition = currentPosition;
      this.watchProgressService.updateProgress(mediaId, profileId, currentPosition).subscribe();
    }
  }
}
