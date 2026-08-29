import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { HeroLink, MediaHero } from '../../components/media-hero/media-hero';
import { TranslocoModule } from '@jsverse/transloco';
import { ActivatedRoute, Router } from '@angular/router';
import { MediaService } from '../../services/media-service';
import { catchError, of } from 'rxjs';
import { MediaBaseResponse } from '../../model/media-base-response';

@Component({
  selector: 'app-unknown-media',
  imports: [MediaHero, TranslocoModule],
  templateUrl: './unknown-media.html',
  styleUrl: './unknown-media.scss',
})
export class UnknownMedia implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router)
  private mediaService = inject(MediaService)

  mediaId = signal<string | null>(null);
  media = signal<MediaBaseResponse | null>(null);

  playLink = computed<HeroLink | null>(() => {
    const item = this.media();
    return item ? ['/media', item.fileInfoId, 'play'] : null;
  });

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
    this.loadMedia();
  }

  loadMedia(): void {
    this.mediaService
      .getMediaBaseById(this.mediaId()!)
      .pipe(
        catchError((error) => {
          console.error('Error fetching media details:', error);
          return of(null);
        })
      )
      .subscribe(item => {
        this.media.set(item);
      });
  }

  navigateToMedia(): void {
    this.mediaService.getMediaBaseById(this.mediaId()!).subscribe({
      next: (item) => {
        this.media.set(item);

        const target: HeroLink | null =
          item.showId != null
            ? ['/shows', item.showId]
            : item.fileInfoId != null
              ? ['/movies', item.fileInfoId]
              : null;

        if (!target) {
          console.warn('Enriched media has neither a show nor a file info id, staying put.');
          return;
        }

        this.router.navigate(target).then((navigated) => {
          if (!navigated) {
            console.warn('Navigation to', target, 'was cancelled or rejected.');
          }
        });
      },
      error: (error) => {
        console.error('Error fetching media details:', error);
      },
    });
  }
}
