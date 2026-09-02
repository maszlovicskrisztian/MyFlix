import { Component, inject, OnInit, signal } from '@angular/core';
import { Header } from '../../components/header/header';
import { MediaSection } from '../../components/media-section/media-section';
import { LoadingOverlay } from '../../components/loading-overlay/loading-overlay';
import { TranslocoModule } from '@jsverse/transloco';
import { catchError, finalize, forkJoin, Observable, of } from 'rxjs';
import { MediaBaseResponse } from '../../model/media-base-response';
import { DiscoverService } from '../../services/discover-service';

@Component({
  selector: 'app-discover',
  imports: [Header, MediaSection, LoadingOverlay, TranslocoModule],
  templateUrl: './discover.html',
  styleUrl: './discover.scss',
})
export class Discover implements OnInit {
  discoverService = inject(DiscoverService);

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
    return this.discoverService.discoverMovies(1).pipe(
      catchError((error) => {
        console.error('Error fetching media items:', error);
        return of([]);
      })
    );
  }

  discoverShows(): Observable<MediaBaseResponse[]> {
    return this.discoverService.discoverShows(1).pipe(
      catchError((error) => {
        console.error('Error fetching unknown media items:', error);
        return of([]);
      })
    );
  }
}
