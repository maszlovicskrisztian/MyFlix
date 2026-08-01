import { Component, inject, OnInit, signal } from '@angular/core';
import { Header } from '../../components/header/header';
import { MediaSection } from "../../components/media-section/media-section";
import { MediaService } from '../../services/media-service';
import { MediaItem } from '../../model/media-item';

@Component({
  selector: 'app-movies',
  imports: [Header, MediaSection],
  templateUrl: './movies.html',
  styleUrl: './movies.scss',
})
export class Movies implements OnInit {
  mediaService = inject(MediaService);
  movies = signal<Array<MediaItem>>([]);
  newMovies = signal<Array<MediaItem>>([]);

  ngOnInit(): void {
    this.mediaService.getMovies().subscribe({
      next: (movies) => {
        this.movies.set(movies);
        var newMovies = movies.sort((a, b) => b.id - a.id).slice(-5);
        this.newMovies.set(newMovies);
      },
      error: (err) => console.error('Hiba a filmek lekérésekor', err),
    });
  }
}
