import { Component, inject, signal } from '@angular/core';
import { Header } from '../../components/header/header';
import { MediaSection } from '../../components/media-section/media-section';
import { ShowService } from '../../services/show-service';
import { MediaBaseResponse } from '../../model/media-base-response';

@Component({
  selector: 'app-shows',
  imports: [Header, MediaSection],
  templateUrl: './shows.html',
  styleUrl: './shows.scss',
})
export class Shows {
  showService = inject(ShowService);
  shows = signal<Array<MediaBaseResponse>>([]);
  newEpisodes = signal<Array<MediaBaseResponse>>([]);

  ngOnInit(): void {
    this.showService.getShows().subscribe({
      next: (shows) => {
        this.shows.set(shows);
        var newEpisodes = shows.sort((a, b) => b.showId - a.showId).slice(-5);
        this.newEpisodes.set(newEpisodes);
      },
      error: (err) => console.error('Hiba a sorozatok lekérésekor', err),
    });
  }
}
