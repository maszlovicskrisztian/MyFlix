import { Component, computed, inject, linkedSignal, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { ShowService } from '../../services/show-service';
import { ShowDetailsResponse } from '../../model/show-details-response';
import { SeasonDetails as SeasonDetailsModel } from '../../model/season-details';
import { EpisodeDetails } from '../../model/episode-details';
import { HeroLink, MediaHero } from '../../components/media-hero/media-hero';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-season-details',
  imports: [RouterLink, MediaHero, TranslocoModule],
  templateUrl: './season-details.html',
  styleUrl: './season-details.scss',
})
export class SeasonDetails implements OnInit {
  private route = inject(ActivatedRoute);
  private showService = inject(ShowService);
  private translocoService = inject(TranslocoService)

  private translation = toSignal(this.translocoService.selectTranslation());

  showId = signal<string | null>(null);
  seasonNumber = signal<number | null>(null);
  show = signal<ShowDetailsResponse | null>(null);

  season = computed<SeasonDetailsModel | null>(() => {
    const number = this.seasonNumber();
    return this.show()?.seasons.find(season => season.seasonNumber === number) ?? null;
  });

  metaItems = computed(() => {
    const item = this.season();

    if (!this.translation()) {
      return [];
    }

    return item ? [`${this.translocoService.translate('SHOW_DETAILS.SEASONS')}: ${item.episodes.length}`] : [];
  });

  playLink = computed<HeroLink | null>(() => {
    const episode = this.season()?.episodes[0];
    return episode ? ['/media', episode.fileInfoId, 'play'] : null;
  });

  private brokenStills = linkedSignal<ShowDetailsResponse | null, Set<number>>({
    source: this.show,
    computation: () => new Set<number>(),
  });

  ngOnInit(): void {
    const seasonNumber = this.route.snapshot.paramMap.get('seasonNumber');

    this.showId.set(this.route.snapshot.paramMap.get('id'));
    this.seasonNumber.set(seasonNumber === null ? null : Number(seasonNumber));
    this.loadShow();
  }

  episodeStillUrl(episode: EpisodeDetails): string | null {
    return this.brokenStills().has(episode.episodeNumber) ? null : episode.stillPath;
  }

  onEpisodeStillError(episode: EpisodeDetails): void {
    this.brokenStills.update(broken => new Set(broken).add(episode.episodeNumber));
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
