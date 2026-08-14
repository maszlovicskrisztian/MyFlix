import { Component, inject, OnInit, signal } from '@angular/core';
import { Header } from '../../components/header/header';
import { MediaSection } from "../../components/media-section/media-section";
import { MovieService } from '../../services/movie-service';
import { MediaBaseResponse } from '../../model/media-base-response';

@Component({
  selector: 'app-movies',
  imports: [Header, MediaSection],
  templateUrl: './movies.html',
  styleUrl: './movies.scss',
})
export class Movies implements OnInit {
  movieService = inject(MovieService);
  movies = signal<Array<MediaBaseResponse>>([]);
  newMovies = signal<Array<MediaBaseResponse>>([]);

  ngOnInit(): void {
    this.movieService.getMovies().subscribe({
      next: (movies) => {
        const newMovies = movies.values().next().value ? Array.from(movies.values()).sort((a, b) => b.fileInfoId! - a.fileInfoId!).slice(0, 5) : [];
        this.movies.set(movies);
        this.newMovies.set(newMovies);
      },
      error: (err) => console.error('Hiba a filmek lekérésekor', err),
    });
  }
}
