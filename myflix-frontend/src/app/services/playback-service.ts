import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { PlaybackInfo } from '../model/playback-info';
import { environment } from '../environments/environment';

@Service()
export class PlaybackService {
    private http = inject(HttpClient);

    getPlaybackInfo(mediaId: number, profileId: number): Observable<PlaybackInfo> {
        const video = document.createElement('video');
        const supportsMkv = video.canPlayType('video/x-matroska') !== '';
        return this.http.get<PlaybackInfo>(
            `${environment.apiUrl}/media/${mediaId}/playback-info?profileId=${profileId}&supportsMkv=${supportsMkv}`
        );
    }
}
