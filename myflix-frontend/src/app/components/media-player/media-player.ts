import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { environment } from '../../../environments/environment.development';

@Component({
  selector: 'app-media-player',
  imports: [],
  templateUrl: './media-player.html',
  styleUrl: './media-player.scss',
})
export class MediaPlayer implements OnInit {
  private route = inject(ActivatedRoute);
  mediaId = signal<string | null>(null);
  streamUrl = computed(() => `${environment.apiUrl}/media/${this.mediaId()}/stream`);

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
  }

}
