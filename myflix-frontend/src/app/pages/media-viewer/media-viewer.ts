import { Component, computed, inject, linkedSignal, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MediaItem } from '../../model/media-item';
import { MediaService } from '../../services/media-service';
import { catchError, of } from 'rxjs';
import { DatePipe } from '@angular/common';
import { Header } from '../../components/header/header';

@Component({
  selector: 'app-media-viewer',
  imports: [RouterLink, DatePipe, Header],
  templateUrl: './media-viewer.html',
  styleUrl: './media-viewer.scss',
})
export class MediaViewer implements OnInit {
  private route = inject(ActivatedRoute);
  private mediaService = inject(MediaService);

  mediaId = signal<string | null>(null);
  mediaItem = signal<MediaItem | null>(null);

  /** Resets whenever a new item arrives, and is cleared when the image fails to load. */
  backdropUrl = linkedSignal(() => this.mediaItem()?.backdropPath ?? null);

  /** Runtime as hh:mm, or null when the metadata is missing. */
  runtime = computed(() => {
    const minutes = this.mediaItem()?.runtimeMinutes;

    if (!minutes || minutes <= 0) {
      return null;
    }

    const hours = Math.floor(minutes / 60);
    return `${String(hours).padStart(2, '0')}:${String(minutes % 60).padStart(2, '0')}`;
  });

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
    this.mediaService
          .getMediaItemById(this.mediaId()!)
          .pipe(
            catchError((error) => {
              console.error('Error fetching media item:', error);
              return of(null);
            })
          )
          .subscribe(item => {
            this.mediaItem.set(item);
        });
  }

  onBackdropError(): void {
    this.backdropUrl.set(null);
  }
}
