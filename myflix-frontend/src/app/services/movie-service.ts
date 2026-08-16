import { inject, Service } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { MediaBaseResponse } from '../model/media-base-response';
import { Observable } from 'rxjs';
import { MovieDetailsResponse } from '../model/movie-details-response';

@Service()
export class MovieService {
    http = inject(HttpClient);

    public getMovies(): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/movies?languageCode=${environment.languageCode}`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }
        
    public getMovieById(id: string): Observable<MovieDetailsResponse> {
        const url = `${environment.apiUrl}/movies/${id}?languageCode=${environment.languageCode}`;
        return this.http.get<MovieDetailsResponse>(url);
    }
    
    public getSuggestedMovies() {
        return Array<MediaBaseResponse>();
    }
}
