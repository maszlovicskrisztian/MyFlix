import { Component, input } from '@angular/core';
import { MediaItem } from '../../model/media-item';
import { MediaCard } from '../media-card/media-card';
import { MediaSectionLayout } from '../../model/media-section-layout';

@Component({
  selector: 'app-media-section',
  imports: [MediaCard],
  templateUrl: './media-section.html',
  styleUrl: './media-section.scss',
})
export class MediaSection {
  layout = input<MediaSectionLayout>('grid');
  title = input('');
  emptyMessage = input('Nincs elérhető tartalom.');
  mediaItems = input<MediaItem[]>([]);
}
