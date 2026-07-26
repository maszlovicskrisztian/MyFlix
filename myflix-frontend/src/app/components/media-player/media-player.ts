import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-media-player',
  imports: [],
  templateUrl: './media-player.html',
  styleUrl: './media-player.scss',
})
export class MediaPlayer implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  mediaId = signal<string | null>(null);

  streamUrl = computed(() => {
    const token = this.authService.getToken();
    return `${environment.apiUrl}/media/${this.mediaId()}/stream?token=${token}`;
  });

  ngOnInit(): void {
    this.mediaId.set(this.route.snapshot.paramMap.get('id'));
  }
}
