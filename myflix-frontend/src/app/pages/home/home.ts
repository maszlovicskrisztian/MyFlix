import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { MediaService } from '../../services/media-service';
import { catchError, finalize, forkJoin, Observable, of } from 'rxjs';
import { Header } from "../../components/header/header";
import { MediaSection } from "../../components/media-section/media-section";
import { LoadingOverlay } from "../../components/loading-overlay/loading-overlay";
import { ProfileService } from '../../services/profile-service';
import { MediaBaseResponse } from '../../model/media-base-response';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-home',
  imports: [Header, MediaSection, LoadingOverlay, TranslocoModule],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  profileService = inject(ProfileService);
  mediaService = inject(MediaService);
  continueWatching = signal<MediaBaseResponse[]>([]);
  suggestedMovies = signal<MediaBaseResponse[]>([]);
  suggestedSeries = signal<MediaBaseResponse[]>([]);
  unknownMedia = signal<MediaBaseResponse[]>([]);
  profileId = computed(() => this.profileService.selectedProfileId());

  loading = signal(true);

  ngOnInit(): void {
    this.loadPage();
  }

  loadPage() {
    this.loading.set(true);

    forkJoin({
      continueWatching: this.getContinueWatching(),
      suggestedMovies: this.getSuggestedMovies(),
      suggestedSeries: this.getSuggestedSeries(),
      unknownMedia: this.getAllUnknownMedia(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe((sections) => {
        this.continueWatching.set(sections.continueWatching);
        this.suggestedMovies.set(sections.suggestedMovies);
        this.suggestedSeries.set(sections.suggestedSeries);
        this.unknownMedia.set(sections.unknownMedia);
      });
  }

  getContinueWatching(): Observable<MediaBaseResponse[]> {
    const profileId = this.profileId();
    if (profileId === null) {
      console.error('Profile ID is undefined. Cannot fetch continue watching items.');
      return of([]);
    }

    return this.mediaService.getContinueWatching(profileId).pipe(
      catchError((error) => {
        console.error('Error fetching media items:', error);
        return of([]);
      })
    );
  }

  getAllUnknownMedia(): Observable<MediaBaseResponse[]> {
    return this.mediaService.getAllUnknownMedia().pipe(
      catchError((error) => {
        console.error('Error fetching unknown media items:', error);
        return of([]);
      })
    );
  }

  getSuggestedMovies(): Observable<MediaBaseResponse[]> {
    return of([]);
  }

  getSuggestedSeries(): Observable<MediaBaseResponse[]> {
    return of([]);
  }
}
