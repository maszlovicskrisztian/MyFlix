import { Component, inject, signal, OnInit } from '@angular/core';
import { MediaService } from '../../services/media-service';
import { MediaItem } from '../../model/media-item';
import { catchError, of } from 'rxjs';
import { Header } from "../../components/header/header";
import { MediaSection } from "../../components/media-section/media-section";

@Component({
  selector: 'app-home',
  imports: [Header, MediaSection],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  mediaService = inject(MediaService);
  continueWatching = signal<MediaItem[]>([]);
  suggestedMovies = signal<MediaItem[]>([]);
  suggestedSeries = signal<MediaItem[]>([]);

  ngOnInit(): void {
    this.getContinueWatching();
    this.getSuggestedMovies();
    this.getSuggestedSeries();
  }

  getContinueWatching() {
    this.mediaService
      .getContinueWatching()
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
