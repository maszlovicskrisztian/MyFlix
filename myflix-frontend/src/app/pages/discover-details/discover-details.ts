import { Component, computed, inject, OnInit, signal, LOCALE_ID } from '@angular/core';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { MediaHero } from '../../components/media-hero/media-hero';
import { ActivatedRoute } from '@angular/router';
import { catchError, of } from 'rxjs';
import { MovieDetailsResponse } from '../../model/movie-details-response';
import { toSignal } from '@angular/core/rxjs-interop';
import { formatDate } from '@angular/common';
import { DiscoverService } from '../../services/discover-service';

@Component({
  selector: 'app-discover-details',
  imports: [MediaHero, TranslocoModule],
  templateUrl: './discover-details.html',
  styleUrl: './discover-details.scss',
})
export class DiscoverDetails implements OnInit {
  private route = inject(ActivatedRoute);
  private discoverService = inject(DiscoverService);
  private translocoService = inject(TranslocoService);
  private locale = inject(LOCALE_ID);
  private translation = toSignal(this.translocoService.selectTranslation());

  tmdbId = signal<string | null>(null);
  movie = signal<MovieDetailsResponse | null>(null);
  
  metaItems = computed(() => {
    const item = this.movie();

    if (!item || !this.translation()) {
      return [];
    }

    return [
      ...(item.releaseDate ? [`${this.translocoService.translate('MOVIE_DETAILS.RELEASE_DATE')}: ${this.longDate(item.releaseDate)}`] : [])
    ];
  });

  runtime = computed(() => {
    const minutes = this.movie()?.runtimeMinutes;

    if (!minutes || minutes <= 0) {
      return null;
    }

    const hours = Math.floor(minutes / 60);
    return `${String(hours).padStart(2, '0')}:${String(minutes % 60).padStart(2, '0')}`;
  });
  
  ngOnInit(): void {
    this.tmdbId.set(this.route.snapshot.paramMap.get('tmdbId'));
    this.loadMovie();
  }

  loadMovie(): void {
    this.discoverService
      .discoveredMovieDetails(this.tmdbId()!)
      .pipe(
        catchError((error) => {
          console.error('Error fetching movie details:', error);
          return of(null);
        })
      )
      .subscribe(item => {
        this.movie.set(item);
      });
  }

  downloadMovie(): void {
    this.discoverService
      .downloadMovie(this.tmdbId()!, this.movie()?.title ?? '', 1)
      .pipe(
        catchError((error) => {
          console.error('Error fetching movie details:', error);
          return of(null);
        })
      )
      .subscribe(item => {
        console.log('Downloading movie');
      });
  }

  private longDate(value: string): string {
    return formatDate(value, 'longDate', this.locale);
  }
}
