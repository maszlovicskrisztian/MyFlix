import { Component, inject, OnInit, signal } from '@angular/core';
import { Header } from '../../components/header/header';
import { MediaSection } from "../../components/media-section/media-section";
import { MovieService } from '../../services/movie-service';
import { MediaBaseResponse } from '../../model/media-base-response';
import { finalize } from 'rxjs';
import { LoadingOverlay } from '../../components/loading-overlay/loading-overlay';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-movies',
  imports: [Header, MediaSection, LoadingOverlay, TranslocoModule],
  templateUrl: './movies.html',
  styleUrl: './movies.scss',
})
export class Movies implements OnInit {
  movieService = inject(MovieService);
  movies = signal<Array<MediaBaseResponse>>([]);
  newMovies = signal<Array<MediaBaseResponse>>([]);
  actionMovies = signal<Array<MediaBaseResponse>>([]);
  loading = signal(true);

  ngOnInit(): void {
    this.loadPage();
  }

  loadPage(): void {
    this.movieService.getMovies()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (movies) => {
          const newMovies = movies.values().next().value ? Array.from(movies.values()).sort((a, b) => b.fileInfoId! - a.fileInfoId!).slice(0, 5) : [];
          const actionMovies = movies.filter((m) => m.genres.includes('Action'));
          this.movies.set(movies);
          this.newMovies.set(newMovies);
          this.actionMovies.set(actionMovies);
        },
        error: (err) => console.error('Hiba a filmek lekérésekor', err),
      });
  }
}
