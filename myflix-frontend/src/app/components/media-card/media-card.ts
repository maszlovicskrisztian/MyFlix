import { Component, computed, input, linkedSignal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MediaBaseResponse } from '../../model/media-base-response';
import { MediaCardAspect } from '../../model/media-card-aspect';

@Component({
  selector: 'app-media-card',
  imports: [RouterLink],
  templateUrl: './media-card.html',
  styleUrl: './media-card.scss',
})
export class MediaCard {
  mediaBase = input<MediaBaseResponse | null>(null);
  routeLink = input<string | null>("/movies");
  route = computed<string>(() => {
    var url = this.routeLink() ?? "";
    
    if (this.mediaBase()?.fileInfoId) {
      url = url + `/${this.mediaBase()?.fileInfoId}`;
    }
    else if (this.mediaBase()?.showId) {
      url = url + `/${this.mediaBase()?.showId}`;
    }
    else if (this.mediaBase()?.tmdbId) {
      url = url + `/${this.mediaBase()?.tmdbId}`;
    }

    if (this.routeLink() == "/media"){
      return url + "/play";
    }

    return url;
  });

  aspect = input<MediaCardAspect>('poster');

  displayTitle = computed(() => {
    const item = this.mediaBase();
    return item?.title || '';
  });

  posterUrl = linkedSignal(() => this.mediaBase()?.imagePath ?? null);

  onPosterError(): void {
    this.posterUrl.set(null);
  }
}
