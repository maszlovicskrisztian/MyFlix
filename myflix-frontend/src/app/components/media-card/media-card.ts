import { Component, computed, input, linkedSignal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MediaBaseResponse } from '../../model/media-base-response';
import { MediaCardAspect } from '../../model/media-card-aspect';

@Component({
  selector: 'app-media-card',
  imports: [RouterLink],
  templateUrl: './media-card.html',
  styleUrl: './media-card.scss',
})
export class MediaCard {
  mediaBase = input<MediaBaseResponse | null>(null);
  routeLink = input<string | null>("/movies");

  /** Shape of the image: a poster by default, 16:9 for lists that hold episodes. */
  aspect = input<MediaCardAspect>('poster');

  displayTitle = computed(() => {
    const item = this.mediaBase();
    return item?.title || 'Nem elérhető cím';
  });

  /** Resets whenever a new item arrives, and is cleared when the image fails to load. */
  posterUrl = linkedSignal(() => this.mediaBase()?.imagePath ?? null);

  onPosterError(): void {
    this.posterUrl.set(null);
  }
}
