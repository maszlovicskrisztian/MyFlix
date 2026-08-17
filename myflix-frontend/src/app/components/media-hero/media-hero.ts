import { Component, input, linkedSignal, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EnrichDialog } from '../enrich-dialog/enrich-dialog';
import { LoadingOverlay } from "../loading-overlay/loading-overlay";

/** A `routerLink` target, e.g. `['/shows', showId()]`. */
export type HeroLink = Array<string | number | null>;

/**
 * The shared top of every details page: backdrop, title, meta, the play/like/enrich
 * actions and the overview. Whatever the page shows below the overview — a season
 * or episode grid, say — is projected in, and stays styled by that page.
 */
@Component({
  selector: 'app-media-hero',
  imports: [RouterLink, EnrichDialog, LoadingOverlay],
  templateUrl: './media-hero.html',
  styleUrl: './media-hero.scss',
})
export class MediaHero {
  /** False while the details are still on their way — a spinner takes the stage. */
  loaded = input(false);

  title = input('');
  overview = input<string | null>(null);
  backdropPath = input<string | null>(null);

  /** Plain lines shown after the HD badge, e.g. `Epizódok száma: 10`. */
  metaItems = input<Array<string>>([]);

  /** Preformatted running time, shown as a pill under the meta line. */
  runtime = input<string | null>(null);

  backLink = input<HeroLink>(['/home']);
  backLabel = input('Vissza a listához');

  /** Where the play button leads; it is hidden when there is nothing to play. */
  playLink = input<HeroLink | null>(null);

  /** What the enrich dialog writes to — usually the media this page is about. */
  enrichMediaId = input<string | null>(null);

  /** Raised once the metadata was rewritten, so the page can reload itself. */
  enriched = output<void>();

  enrichDialogOpen = signal(false);

  /** Resets whenever a new backdrop arrives, and is cleared when it fails to load. */
  backdropUrl = linkedSignal(() => this.backdropPath());

  onBackdropError(): void {
    this.backdropUrl.set(null);
  }
}
