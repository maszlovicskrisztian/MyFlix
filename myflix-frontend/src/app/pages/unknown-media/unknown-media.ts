import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { HeroLink, MediaHero } from '../../components/media-hero/media-hero';
import { TranslocoModule } from '@jsverse/transloco';
import { ActivatedRoute } from '@angular/router';
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
  private mediaService = inject(MediaService)

  mediaId = signal<string | null>(null);
  media = signal<MediaBaseResponse | null>(null);

  playLink = computed<HeroLink | null>(() => {
    const item = this.media();
    return item ? ['/media', item.fileInfoId, 'play'] : null;
  });

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
    this.loadMovie();
  }

  loadMovie(): void {
    this.mediaService
      .getUnknownMediaById(this.mediaId()!)
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
}
