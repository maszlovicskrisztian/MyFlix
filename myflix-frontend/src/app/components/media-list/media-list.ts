import { Component, inject, signal, OnInit } from '@angular/core';
import { MediaService } from '../../services/media-service';
import { MediaItem } from '../../model/media-item';
import { catchError, of } from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-media-list',
  imports: [RouterLink],
  templateUrl: './media-list.html',
  styleUrls: ['./media-list.scss'],
})
export class MediaList implements OnInit {
  mediaService = inject(MediaService);
  mediaItems = signal<MediaItem[]>([]);

  ngOnInit(): void {
    this.mediaService
      .getMediaItems()
      .pipe(
        catchError((error) => {
          console.error('Error fetching media items:', error);
          return of([]);
        })
      )
      .subscribe(items => {
        this.mediaItems.set(items);
    });
  }
}
