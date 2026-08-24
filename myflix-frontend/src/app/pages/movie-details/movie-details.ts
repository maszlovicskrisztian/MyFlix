import { Component, computed, inject, LOCALE_ID, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { formatDate } from '@angular/common';
import { catchError, of } from 'rxjs';
import { MovieService } from '../../services/movie-service';
import { MovieDetailsResponse } from '../../model/movie-details-response';
import { HeroLink, MediaHero } from '../../components/media-hero/media-hero';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-movie-details',
  imports: [MediaHero, TranslocoModule],
  templateUrl: './movie-details.html',
  styleUrl: './movie-details.scss',
})
export class MovieDetails implements OnInit {
  private route = inject(ActivatedRoute);
  private movieService = inject(MovieService);
  private locale = inject(LOCALE_ID);
  private translocoService = inject(TranslocoService);

  private translation = toSignal(this.translocoService.selectTranslation());

  movieId = signal<string | null>(null);
  movie = signal<MovieDetailsResponse | null>(null);

  metaItems = computed(() => {
    const item = this.movie();

    if (!item || !this.translation()) {
      return [];
    }

    return [
      ...(item.releaseDate ? [`${this.translocoService.translate('MOVIE_DETAILS.RELEASE_DATE')}: ${this.longDate(item.releaseDate)}`] : []),
      `${this.translocoService.translate('MOVIE_DETAILS.ADDED_AT')}: ${this.longDate(item.addedAt)}`,
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

  playLink = computed<HeroLink | null>(() => {
    const item = this.movie();
    return item ? ['/media', item.id, 'play'] : null;
  });

  ngOnInit(): void {
    this.movieId.set(this.route.snapshot.paramMap.get('id'));
    this.loadMovie();
  }

  loadMovie(): void {
    this.movieService
      .getMovieById(this.movieId()!)
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

  private longDate(value: string): string {
    return formatDate(value, 'longDate', this.locale);
  }
}
