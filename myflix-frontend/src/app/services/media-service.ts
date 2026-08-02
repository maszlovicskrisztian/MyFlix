import { inject, Service } from '@angular/core';
import { MediaItem } from '../model/media-item';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Service()
export class MediaService {
    http = inject(HttpClient);

    public getContinueWatching(profileId: number) {
        const url = `${environment.apiUrl}/media/continue-watching?profileId=${profileId}`;
        return this.http.get<Array<MediaItem>>(url);
    }

    public getMovies() {
        const url = `${environment.apiUrl}/media/movies`;
        return this.http.get<Array<MediaItem>>(url);
    }

    public getSuggestedMovies() {
        return Array<MediaItem>();
    }

    public getSuggestedSeries() {
        return Array<MediaItem>();
    }

    public getMediaItemById(id: string) {
        const url = `${environment.apiUrl}/media/${id}`;
        return this.http.get<MediaItem>(url);
    }

    public enrichMetadata() {
        const url = `${environment.apiUrl}/metadata/enrich`;
        return this.http.post<void>(url, {});
    }

    public getAllMedia() {
        const url = `${environment.apiUrl}/media`;
        return this.http.get<Array<MediaItem>>(url);
    }
}