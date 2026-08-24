import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { WatchProgressResponse } from '../model/watch-progress-response';
import { Observable } from 'rxjs';
import { PlaybackInfo } from '../model/playback-info';
import { MediaBaseResponse } from '../model/media-base-response';
import { MediaSearchResponse } from '../model/media-search-response';
import { LanguageService } from './language-service';

@Service()
export class MediaService {
    private http = inject(HttpClient);
    private languageService = inject(LanguageService);
    
    public getAllUnknownMedia(): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/media/unknown`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }
    
    public getUnknownMediaById(mediaId: string): Observable<MediaBaseResponse> {
        const url = `${environment.apiUrl}/media/unknown/${mediaId}`;
        return this.http.get<MediaBaseResponse>(url);
    }

    public getContinueWatching(profileId: number): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/media/continue-watching?profileId=${profileId}&languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }
    
    public getProgress(mediaId: number, profileId: number): Observable<WatchProgressResponse> {
        const url = `${environment.apiUrl}/media/${mediaId}/progress?profileId=${profileId}`;
        return this.http.get<WatchProgressResponse>(url);
    }

    public updateProgress(mediaId: number, profileId: number, progressSeconds: number): Observable<WatchProgressResponse> {
        const url = `${environment.apiUrl}/media/${mediaId}/progress?profileId=${profileId}`;
        return this.http.put<WatchProgressResponse>(url, { progressSeconds });
    }
    
    public getPlaybackInfo(mediaId: number, profileId: number): Observable<PlaybackInfo> {
        const video = document.createElement('video');
        const supportsMkv = video.canPlayType('video/x-matroska') !== '';
        return this.http.get<PlaybackInfo>(
            `${environment.apiUrl}/media/${mediaId}/playback-info?profileId=${profileId}&supportsMkv=${supportsMkv}`
        );
    }

    public searchMedia(query: string): Observable<Array<MediaSearchResponse>> {
        const url = `${environment.apiUrl}/media/search?query=${encodeURIComponent(query)}&languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<Array<MediaSearchResponse>>(url);
    }
}