import { inject, Service } from '@angular/core';
import { MediaItem } from '../model/media-item';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { MediaBaseResponse } from '../model/media-base-response';
import { Observable } from 'rxjs';
import { ShowDetailsResponse } from '../model/show-details-response';

@Service()
export class ShowService {
    http = inject(HttpClient);
    
    public getShows(): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/shows`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }

    public getShowById(id: string): Observable<ShowDetailsResponse> {
        const url = `${environment.apiUrl}/shows/${id}`;
        return this.http.get<ShowDetailsResponse>(url);
    }
    
    public getSuggestedSeries() {
        return Array<MediaItem>();
    }
}
