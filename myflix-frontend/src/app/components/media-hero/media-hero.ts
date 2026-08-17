import { Component, input, linkedSignal, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EnrichDialog } from '../enrich-dialog/enrich-dialog';
import { LoadingOverlay } from "../loading-overlay/loading-overlay";
import { TranslocoModule } from '@jsverse/transloco';

/** A `routerLink` target, e.g. `['/shows', showId()]`. */
export type HeroLink = Array<string | number | null>;

/**
 * The shared top of every details page: backdrop, title, meta, the play/like/enrich
 * actions and the overview. Whatever the page shows below the overview — a season
 * or episode grid, say — is projected in, and stays styled by that page.
 */
@Component({
  selector: 'app-media-hero',
  imports: [RouterLink, EnrichDialog, LoadingOverlay, TranslocoModule],
  templateUrl: './media-hero.html',
  styleUrl: './media-hero.scss',
})
export class MediaHero {
  loaded = input(false);

  title = input('');
  overview = input<string | null>(null);
  backdropPath = input<string | null>(null);

  metaItems = input<Array<string>>([]);

  runtime = input<string | null>(null);

  backLink = input<HeroLink>(['/home']);
  backLabel = input('Vissza a listához');

  playLink = input<HeroLink | null>(null);

  enrichMediaId = input<string | null>(null);

  enriched = output<void>();

  enrichDialogOpen = signal(false);

  backdropUrl = linkedSignal(() => this.backdropPath());

  onBackdropError(): void {
    this.backdropUrl.set(null);
  }
}
