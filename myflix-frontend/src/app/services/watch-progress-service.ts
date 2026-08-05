import { inject, Service } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { WatchProgressResponse } from '../model/watch-progress-response';

@Service()
export class WatchProgressService {
    http = inject(HttpClient);
    
    public getProgress(mediaId: number, profileId: number) {
        const url = `${environment.apiUrl}/media/${mediaId}/progress?profileId=${profileId}`;
        return this.http.get<WatchProgressResponse>(url);
    }

    public updateProgress(mediaId: number, profileId: number, progressSeconds: number) {
        const url = `${environment.apiUrl}/media/${mediaId}/progress?profileId=${profileId}`;
        return this.http.put(url, { progressSeconds });
    }
}
