import {
  Component,
  ElementRef,
  HostListener,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { MediaBaseResponse } from '../../model/media-base-response';
import { ManualMetadataRequest } from '../../model/manual-metadata-request';
import { MetadataDetails } from '../../model/metadata-details';
import { MetadataService } from '../../services/metadata-service';
import { ShowService } from '../../services/show-service';

/** Which half of the form is showing; also the media type we send. */
export type MetadataEditorMode = 'MOVIE' | 'EPISODE';

/**
 * Fills a title in by hand when enrichment can't find it, and corrects one it got
 * wrong: opening the dialog loads whatever is already stored for the media and fills
 * the form with it. The shared fields sit at the top and the mode switch swaps the
 * poster field for the show/season/episode ones. Opened from the enrich dialog and
 * from the per-episode action on a season page.
 */
@Component({
  selector: 'app-metadata-editor',
  imports: [FormsModule, TranslocoModule],
  templateUrl: './metadata-editor.html',
  styleUrl: './metadata-editor.scss',
})
export class MetadataEditor {
  private metadataService = inject(MetadataService);
  private showService = inject(ShowService);
  private translocoService = inject(TranslocoService);

  open = model(false);
  mediaId = input<string | null>(null);
  saved = output<void>();

  busy = signal(false);
  loading = signal(false);
  shows = signal<Array<MediaBaseResponse>>([]);

  disabled = computed(() => this.busy() || this.loading());

  mode = linkedSignal<boolean, MetadataEditorMode>({
    source: this.open,
    computation: () => 'MOVIE',
  });

  tmdbId = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  title = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  overview = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  backdropPath = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  releaseDate = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  runtimeMinutes = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  genres = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });

  posterPath = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });

  showId = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  seasonNumber = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  episodeNumber = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });

  error = linkedSignal<boolean, string | null>({ source: this.open, computation: () => null });

  private titleInput = viewChild<ElementRef<HTMLInputElement>>('titleInput');

  constructor() {
    effect(() => this.titleInput()?.nativeElement.focus());

    effect(() => {
      if (!this.open() || this.shows().length) {
        return;
      }

      this.showService.getShows().subscribe({
        next: (shows) => this.shows.set(shows),
        error: (error) => console.error('Error loading shows:', error),
      });
    });

    effect(() => {
      const mediaId = this.mediaId();

      if (!this.open() || !mediaId) {
        return;
      }

      this.loading.set(true);

      this.metadataService.getMetadata(mediaId).subscribe({
        next: (details) => {
          this.loading.set(false);

          if (!this.open() || this.mediaId() !== mediaId) {
            return;
          }

          this.prefill(details);
        },
        error: (error) => {
          console.error('Error loading metadata:', error);
          this.loading.set(false);
        },
      });
    });
  }

  setMode(mode: MetadataEditorMode): void {
    if (this.disabled()) {
      return;
    }

    this.mode.set(mode);
    this.error.set(null);
  }

  submit(): void {
    const mediaId = this.mediaId();

    if (!mediaId) {
      this.error.set(this.translocoService.translate('METADATA_EDITOR.ERRORS.MISSING_MEDIA_ID'));
      return;
    }

    if (!this.title().trim()) {
      this.error.set(this.translocoService.translate('METADATA_EDITOR.ERRORS.MISSING_TITLE'));
      return;
    }

    const episode = this.mode() === 'EPISODE';

    if (episode && !this.showId()) {
      this.error.set(this.translocoService.translate('METADATA_EDITOR.ERRORS.MISSING_SHOW'));
      return;
    }

    if (episode && (this.season() === null || this.episode() === null)) {
      this.error.set(this.translocoService.translate('METADATA_EDITOR.ERRORS.MISSING_EPISODE_NUMBERS'));
      return;
    }

    this.error.set(null);
    this.busy.set(true);

    this.metadataService.saveManualMetadata(mediaId, this.buildRequest()).subscribe({
      next: () => {
        this.busy.set(false);
        this.open.set(false);
        this.saved.emit();
      },
      error: (error) => {
        console.error('Error saving metadata:', error);
        this.busy.set(false);
        this.error.set(this.translocoService.translate('METADATA_EDITOR.ERRORS.GENERIC'));
      },
    });
  }

  cancel(): void {
    if (this.busy()) {
      return;
    }

    this.open.set(false);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (!this.open()) {
      return;
    }

    this.cancel();
  }

  private prefill(details: MetadataDetails): void {
    this.mode.set(details.mediaType ?? 'MOVIE');

    this.tmdbId.set(this.numberText(details.tmdbId));
    this.title.set(details.title ?? '');
    this.overview.set(details.overview ?? '');
    this.backdropPath.set(details.backdropPath ?? '');
    this.releaseDate.set(details.releaseDate ?? '');
    this.runtimeMinutes.set(this.numberText(details.runtimeMinutes));
    this.genres.set((details.genres ?? []).join(', '));

    this.posterPath.set(details.posterPath ?? '');

    this.showId.set(this.numberText(details.showId));
    this.seasonNumber.set(this.numberText(details.seasonNumber));
    this.episodeNumber.set(this.numberText(details.episodeNumber));
  }

  private buildRequest(): ManualMetadataRequest {
    const episode = this.mode() === 'EPISODE';

    return {
      mediaType: this.mode(),
      tmdbId: this.number(this.tmdbId()),
      title: this.title().trim(),
      overview: this.text(this.overview()),
      backdropPath: this.text(this.backdropPath()),
      releaseDate: this.text(this.releaseDate()),
      runtimeMinutes: this.number(this.runtimeMinutes()),
      genres: this.genres()
        .split(',')
        .map((genre) => genre.trim())
        .filter((genre) => genre.length > 0),
      posterPath: episode ? null : this.text(this.posterPath()),
      showId: episode ? this.number(this.showId()) : null,
      seasonNumber: episode ? this.season() : null,
      episodeNumber: episode ? this.episode() : null,
    };
  }

  private season(): number | null {
    return this.number(this.seasonNumber());
  }

  private episode(): number | null {
    return this.number(this.episodeNumber());
  }

  private text(value: string): string | null {
    return value.trim() || null;
  }

  private number(value: string): number | null {
    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (!trimmed) {
        return null;
      }
      const parsed = Number(trimmed);
      return Number.isFinite(parsed) ? parsed : null;
    }

    return Number(value);
  }

  private numberText(value: number | null): string {
    return value === null || value === undefined ? '' : String(value);
  }
}
