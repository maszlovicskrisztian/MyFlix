import { Component, computed, input, linkedSignal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MediaItem } from '../../model/media-item';

@Component({
  selector: 'app-media-card',
  imports: [RouterLink],
  templateUrl: './media-card.html',
  styleUrl: './media-card.scss',
})
export class MediaCard {
  mediaItem = input<MediaItem | null>(null);

  displayTitle = computed(() => {
    const item = this.mediaItem();
    return item?.title || item?.fileName || '';
  });

  /** Resets whenever a new item arrives, and is cleared when the image fails to load. */
  posterUrl = linkedSignal(() => this.mediaItem()?.posterPath ?? null);

  onPosterError(): void {
    this.posterUrl.set(null);
  }
}
