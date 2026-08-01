import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { MediaService } from '../../services/media-service';
import { MediaItem } from '../../model/media-item';
import { catchError, of } from 'rxjs';
import { Header } from "../../components/header/header";
import { MediaSection } from "../../components/media-section/media-section";
import { ProfileService } from '../../services/profile-service';

@Component({
  selector: 'app-home',
  imports: [Header, MediaSection],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  profileService = inject(ProfileService);
  mediaService = inject(MediaService);
  continueWatching = signal<MediaItem[]>([]);
  suggestedMovies = signal<MediaItem[]>([]);
  suggestedSeries = signal<MediaItem[]>([]);
  allMedia = signal<MediaItem[]>([]);
  profileId = computed(() => this.profileService.selectedProfileId());

  ngOnInit(): void {
    this.getContinueWatching();
    this.getSuggestedMovies();
    this.getSuggestedSeries();
    this.getAllMedia();
  }

  getContinueWatching() {
    const profileId = this.profileId();
    if (profileId === null) {
      console.error('Profile ID is undefined. Cannot fetch continue watching items.');
      return;
    }

    this.mediaService
      .getContinueWatching(profileId)
      .pipe(
        catchError((error) => {
          console.error('Error fetching media items:', error);
          return of([]);
        })
      )
      .subscribe(items => {
        this.continueWatching.set(items);
    });
  }

  getAllMedia() {
    this.mediaService
      .getAllMedia()
      .subscribe({
        next: (items) => { this.allMedia.set(items); },
        error: (error) => {
          console.error('Error fetching all media items:', error);
          this.allMedia.set([]);
        }
      }
    );
  }

  getSuggestedMovies() {
    // this.mediaService
    //   .getSuggestedMovies()
    //   .pipe(
    //     catchError((error) => {
    //       console.error('Error fetching media items:', error);
    //       return of([]);
    //     })
    //   )
    //   .subscribe(items => {
    //     this.suggestedMovies.set(items);
    // });
  }

  getSuggestedSeries() {
  //   this.mediaService
  //     .getSuggestedSeries()
  //     .pipe(
  //       catchError((error) => {
  //         console.error('Error fetching media items:', error);
  //         return of([]);
  //       })
  //     )
  //     .subscribe(items => {
  //       this.suggestedSeries.set(items);
  //   });
  }
}
