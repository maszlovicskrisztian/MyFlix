import { Component, computed, inject, linkedSignal, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MediaItem } from '../../model/media-item';
import { MediaService } from '../../services/media-service';
import { catchError, of } from 'rxjs';
import { DatePipe } from '@angular/common';
import { MetadataService } from '../../services/metadata-service';
import { EnrichDialog } from '../../components/enrich-dialog/enrich-dialog';

@Component({
  selector: 'app-media-viewer',
  imports: [RouterLink, DatePipe, EnrichDialog],
  templateUrl: './media-viewer.html',
  styleUrl: './media-viewer.scss',
})
export class MediaViewer implements OnInit {
  private route = inject(ActivatedRoute);
  private mediaService = inject(MediaService);
  private metadataService = inject(MetadataService);

  mediaId = signal<string | null>(null);
  mediaItem = signal<MediaItem | null>(null);
  enriching = signal(false);
  enrichDialogOpen = signal(false);

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
    this.loadMediaItem();
  }

  openEnrichDialog(): void {
    this.enrichDialogOpen.set(true);
  }

  closeEnrichDialog(): void {
    this.enrichDialogOpen.set(false);
  }

  enrich(imdbId: string): void {
    this.enriching.set(true);

    this.metadataService
      .enrichByImdbId(this.mediaId()!, imdbId)
      .pipe(
        catchError((error) => {
          console.error('Error enriching metadata:', error);
          return of(null);
        })
      )
      .subscribe(() => {
        this.enriching.set(false);
        this.enrichDialogOpen.set(false);
        this.loadMediaItem();
      });
  }

  onBackdropError(): void {
    this.backdropUrl.set(null);
  }

  private loadMediaItem(): void {
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
}
