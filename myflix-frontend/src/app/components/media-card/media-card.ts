import { Component, input } from '@angular/core';
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
}
