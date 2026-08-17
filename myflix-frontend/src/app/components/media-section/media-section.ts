import { Component, computed, inject, input, linkedSignal } from '@angular/core';
import { MediaCard } from '../media-card/media-card';
import { MediaSectionLayout } from '../../model/media-section-layout';
import { MediaBaseResponse } from '../../model/media-base-response';
import { MediaCardAspect } from '../../model/media-card-aspect';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-media-section',
  imports: [MediaCard, TranslocoModule],
  templateUrl: './media-section.html',
  styleUrl: './media-section.scss',
})
export class MediaSection {
  private translocoService = inject(TranslocoService);
  
  layout = input<MediaSectionLayout>('grid');

  aspect = input<MediaCardAspect>('poster');
  title = input('');
  emptyMessage = input(this.translocoService.translate('MEDIA_SECTION.EMPTY_TITLE'));
  routeLink = input<string | null>("/movies");
  mediaBaseResponses = input<Array<MediaBaseResponse>>([]);

  maxCapacity = input(0);
  paginated = computed(
    () =>
      this.layout() === 'grid' &&
      this.maxCapacity() > 0 &&
      this.mediaBaseResponses().length > this.maxCapacity()
  );

  pageCount = computed(() =>
    this.paginated() ? Math.ceil(this.mediaBaseResponses().length / this.maxCapacity()) : 1
  );

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
