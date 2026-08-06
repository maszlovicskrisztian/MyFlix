import { Component, input } from '@angular/core';
import { MediaCard } from '../media-card/media-card';
import { MediaSectionLayout } from '../../model/media-section-layout';
import { MediaBaseResponse } from '../../model/media-base-response';
import { MediaCardAspect } from '../../model/media-card-aspect';

@Component({
  selector: 'app-media-section',
  imports: [MediaCard],
  templateUrl: './media-section.html',
  styleUrl: './media-section.scss',
})
export class MediaSection {
  layout = input<MediaSectionLayout>('grid');

  /** Handed to every card, and widens the tiles to match the 16:9 shape. */
  aspect = input<MediaCardAspect>('poster');
  title = input('');
  emptyMessage = input('Nincs elérhető tartalom.');
  routeLink = input<string | null>("/movies");
  mediaBaseResponses = input<Array<MediaBaseResponse>>([]);
}
