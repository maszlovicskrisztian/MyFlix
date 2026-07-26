import { inject, Service } from '@angular/core';
import { MediaItem } from '../model/media-item';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Service()
export class MediaService {
    http = inject(HttpClient);

    public getMediaItems() {
        const url = `${environment.apiUrl}/media`;
        return this.http.get<Array<MediaItem>>(url);
    }

    public getMediaItemById(id: string) {
        const url = `${environment.apiUrl}/media/${id}`;
        return this.http.get<MediaItem>(url);
    }
}