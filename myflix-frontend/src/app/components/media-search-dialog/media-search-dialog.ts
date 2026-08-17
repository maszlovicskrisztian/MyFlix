import {
  Component,
  ElementRef,
  HostListener,
  effect,
  inject,
  linkedSignal,
  model,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, map, of, switchMap } from 'rxjs';
import { MediaService } from '../../services/media-service';
import { MediaSearchResponse } from '../../model/media-search-response';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

const SEARCH_DEBOUNCE_MS = 250;
const MIN_QUERY_LENGTH = 2;

type SearchOutcome = {
  results: Array<MediaSearchResponse>;
  searched: boolean;
  failed: boolean;
};

@Component({
  selector: 'app-media-search-dialog',
  imports: [FormsModule, TranslocoModule],
  templateUrl: './media-search-dialog.html',
  styleUrl: './media-search-dialog.scss',
})
export class MediaSearchDialog {
  private mediaService = inject(MediaService);
  private router = inject(Router);
  private translocoService = inject(TranslocoService);

  open = model(false);

  busy = signal(false);

  query = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  results = linkedSignal<boolean, Array<MediaSearchResponse>>({
    source: this.open,
    computation: () => [],
  });
  error = linkedSignal<boolean, string | null>({ source: this.open, computation: () => null });
  searched = linkedSignal<boolean, boolean>({ source: this.open, computation: () => false });

  private queryInput = viewChild<ElementRef<HTMLInputElement>>('queryInput');
  private queries = new Subject<string>();

  constructor() {
    effect(() => this.queryInput()?.nativeElement.focus());

    effect(() => {
      this.open();
      this.queries.next('');
    });

    this.queries
      .pipe(
        debounceTime(SEARCH_DEBOUNCE_MS),
        distinctUntilChanged(),
        switchMap((query) => {
          if (query.length < MIN_QUERY_LENGTH) {
            return of<SearchOutcome>({ results: [], searched: false, failed: false });
          }

          this.busy.set(true);

          return this.mediaService.searchMedia(query).pipe(
            map((results): SearchOutcome => ({ results, searched: true, failed: false })),
            catchError((error) => {
              console.error('Error searching media:', error);
              return of<SearchOutcome>({ results: [], searched: true, failed: true });
            })
          );
        }),
        takeUntilDestroyed()
      )
      .subscribe(({ results, searched, failed }) => {
        this.busy.set(false);
        this.results.set(results);
        this.searched.set(searched);
        this.error.set(failed ? this.translocoService.translate('SEARCHBAR.ERROR') : null);
      });
  }

  onQueryChange(query: string): void {
    this.query.set(query);
    this.queries.next(query.trim());
  }

  select(result: MediaSearchResponse): void {
    this.close();
    this.router.navigate([result.mediaType === 'TV' ? '/shows' : '/movies', result.mediaId]);
  }

  close(): void {
    this.open.set(false);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (!this.open()) {
      return;
    }

    this.close();
  }
}
