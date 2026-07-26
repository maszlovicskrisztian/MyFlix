import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MediaItem } from '../../model/media-item';
import { MediaService } from '../../services/media-service';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-media-viewer',
  imports: [RouterLink],
  templateUrl: './media-viewer.html',
  styleUrl: './media-viewer.scss',
})
export class MediaViewer implements OnInit {
  private route = inject(ActivatedRoute);
  private mediaService = inject(MediaService);

  mediaId = signal<string | null>(null);
  mediaItem = signal<MediaItem | null>(null);

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
    this.mediaService
          .getMediaItemById(this.mediaId()!)
          .pipe(
            catchError((error) => {
              console.error('Error fetching media item:', error);
              return of(null);
            })
          )
          .subscribe(item => {
            this.mediaItem.set(item);
        });
  }
}
