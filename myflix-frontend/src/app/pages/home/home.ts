import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { MediaService } from '../../services/media-service';
import { catchError, of } from 'rxjs';
import { Header } from "../../components/header/header";
import { MediaSection } from "../../components/media-section/media-section";
import { ProfileService } from '../../services/profile-service';
import { MediaBaseResponse } from '../../model/media-base-response';

@Component({
  selector: 'app-home',
  imports: [Header, MediaSection],
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

  ngOnInit(): void {
    this.getContinueWatching();
    this.getSuggestedMovies();
    this.getSuggestedSeries();
    this.getAllUnknownMedia();
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

  getAllUnknownMedia() {
    this.mediaService
      .getAllUnknownMedia()
      .subscribe({
        next: (items) => { this.unknownMedia.set(items); },
        error: (error) => {
          console.error('Error fetching unknown media items:', error);
          this.unknownMedia.set([]);
        }
      }
    );
  }

  getSuggestedMovies() {
  }

  getSuggestedSeries() {
  }
}
