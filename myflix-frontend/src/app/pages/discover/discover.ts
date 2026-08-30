import { Component, inject, OnInit, signal } from '@angular/core';
import { Header } from '../../components/header/header';
import { MediaSection } from '../../components/media-section/media-section';
import { LoadingOverlay } from '../../components/loading-overlay/loading-overlay';
import { TranslocoModule } from '@jsverse/transloco';
import { catchError, finalize, forkJoin, Observable, of } from 'rxjs';
import { MovieService } from '../../services/movie-service';
import { ShowService } from '../../services/show-service';
import { MediaBaseResponse } from '../../model/media-base-response';

@Component({
  selector: 'app-discover',
  imports: [Header, MediaSection, LoadingOverlay, TranslocoModule],
  templateUrl: './discover.html',
  styleUrl: './discover.scss',
})
export class Discover implements OnInit {
  movieService = inject(MovieService);
  showService = inject(ShowService);

  discoveredMovies = signal<MediaBaseResponse[]>([]);
  discoveredShows = signal<MediaBaseResponse[]>([]);

  loading = signal(true);

  ngOnInit(): void {
    this.loadPage();
  }

  loadPage() {
    this.loading.set(true);

    forkJoin({
      discoveredMovies: this.discoverMovies(),
      discoveredShows: this.discoverShows(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe((sections) => {
        this.discoveredMovies.set(sections.discoveredMovies);
        this.discoveredShows.set(sections.discoveredShows);
      });
  }
  
  discoverMovies(): Observable<MediaBaseResponse[]> {
    return this.movieService.discoverMovies(1).pipe(
      catchError((error) => {
        console.error('Error fetching media items:', error);
        return of([]);
      })
    );
  }

  discoverShows(): Observable<MediaBaseResponse[]> {
    return this.showService.discoverMovies(1).pipe(
      catchError((error) => {
        console.error('Error fetching unknown media items:', error);
        return of([]);
      })
    );
  }
}
