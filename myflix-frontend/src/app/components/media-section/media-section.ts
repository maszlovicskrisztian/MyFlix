import { Component, computed, input, linkedSignal } from '@angular/core';
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

  /** How many items fit on one page; 0 (the default) puts them all on one. */
  maxCapacity = input(0);

  /** Only the grid pages — the row layout already scrolls horizontally. */
  paginated = computed(
    () =>
      this.layout() === 'grid' &&
      this.maxCapacity() > 0 &&
      this.mediaBaseResponses().length > this.maxCapacity()
  );

  pageCount = computed(() =>
    this.paginated() ? Math.ceil(this.mediaBaseResponses().length / this.maxCapacity()) : 1
  );

  /** Back to the first page whenever the items or the capacity change under it. */
  pageIndex = linkedSignal({
    source: () => ({ items: this.mediaBaseResponses(), capacity: this.maxCapacity() }),
    computation: () => 0,
  });

  visibleItems = computed(() => {
    if (!this.paginated()) {
      return this.mediaBaseResponses();
    }
    const start = this.pageIndex() * this.maxCapacity();
    return this.mediaBaseResponses().slice(start, start + this.maxCapacity());
  });

  previousPage(): void {
    this.pageIndex.update((index) => Math.max(0, index - 1));
  }

  nextPage(): void {
    this.pageIndex.update((index) => Math.min(this.pageCount() - 1, index + 1));
  }
}
