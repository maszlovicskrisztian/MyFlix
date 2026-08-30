import { inject, Service } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { MediaBaseResponse } from '../model/media-base-response';
import { Observable } from 'rxjs';
import { MovieDetailsResponse } from '../model/movie-details-response';
import { LanguageService } from './language-service';

@Service()
export class MovieService {
    private http = inject(HttpClient);
    private languageService = inject(LanguageService);

    public getMovies(): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/movies?languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }
        
    public getMovieById(id: string): Observable<MovieDetailsResponse> {
        const url = `${environment.apiUrl}/movies/${id}?languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<MovieDetailsResponse>(url);
    }
    
    public discoverMovies(monthsBack: number): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/movies/discover?monthsBack=${monthsBack}&languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }
    
    public getSuggestedMovies() {
        return Array<MediaBaseResponse>();
    }
}
