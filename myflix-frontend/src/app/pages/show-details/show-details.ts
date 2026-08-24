import { Component, computed, inject, linkedSignal, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { ShowService } from '../../services/show-service';
import { ShowDetailsResponse } from '../../model/show-details-response';
import { SeasonDetails } from '../../model/season-details';
import { HeroLink, MediaHero } from '../../components/media-hero/media-hero';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-show-details',
  imports: [RouterLink, MediaHero, TranslocoModule],
  templateUrl: './show-details.html',
  styleUrl: './show-details.scss',
})
export class ShowDetails implements OnInit {
  private route = inject(ActivatedRoute);
  private showService = inject(ShowService);
  private translocoService = inject(TranslocoService)

  private translation = toSignal(this.translocoService.selectTranslation());

  showId = signal<string | null>(null);
  show = signal<ShowDetailsResponse | null>(null);

  metaItems = computed(() => {
    const item = this.show();

    if (!item || !this.translation()) {
      return [];
    }
    const seasons = `${this.translocoService.translate('SHOW_DETAILS.SEASON_COUNT')}: ${item.seasonCount}`
    const episodes = `${this.translocoService.translate('SHOW_DETAILS.EPISODE_COUNT')}: ${item.episodeCount}`
    return [seasons, episodes];
  });

  playLink = computed<HeroLink | null>(() => {
    const item = this.show()?.seasons[0]?.episodes[0];
    return item ? ['/media', item.fileInfoId, 'play'] : null;
  });

  private brokenSeasonPosters = linkedSignal<ShowDetailsResponse | null, Set<number>>({
    source: this.show,
    computation: () => new Set<number>(),
  });

  ngOnInit(): void {
    this.showId.set(this.route.snapshot.paramMap.get('id'));
    this.loadShow();
  }

  seasonPosterUrl(season: SeasonDetails): string | null {
    return this.brokenSeasonPosters().has(season.seasonNumber) ? null : season.posterPath;
  }

  onSeasonPosterError(season: SeasonDetails): void {
    this.brokenSeasonPosters.update(broken => new Set(broken).add(season.seasonNumber));
  }

  loadShow(): void {
    this.showService
      .getShowById(this.showId()!)
      .pipe(
        catchError((error) => {
          console.error('Error fetching show details:', error);
          return of(null);
        })
      )
      .subscribe(item => {
        this.show.set(item);
      });
  }
}
