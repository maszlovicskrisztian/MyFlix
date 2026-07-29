import { Component, input, signal } from '@angular/core';
import { MediaItem } from '../../model/media-item';
import { MediaCard } from '../media-card/media-card';

@Component({
  selector: 'app-media-section',
  imports: [MediaCard],
  templateUrl: './media-section.html',
  styleUrl: './media-section.scss',
})
export class MediaSection {
  title = input('');
  mediaItems = input<MediaItem[]>([]);
  emptyMessage = input('Nincs elérhető tartalom.');
}
