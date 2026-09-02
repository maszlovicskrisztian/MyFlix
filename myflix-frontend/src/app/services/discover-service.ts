import { inject, Service } from '@angular/core';
import { MovieDetailsResponse } from '../model/movie-details-response';
import { environment } from '../environments/environment';
import { Observable } from 'rxjs';
import { MediaBaseResponse } from '../model/media-base-response';
import { LanguageService } from './language-service';
import { HttpClient } from '@angular/common/http';

@Service()
export class DiscoverService {
    private http = inject(HttpClient);
    private languageService = inject(LanguageService);
    
    public discoverMovies(monthsBack: number): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/discover/movies?monthsBack=${monthsBack}&languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }

    public discoveredMovieDetails(tmdbId: string): Observable<MovieDetailsResponse> {
        const url = `${environment.apiUrl}/discover/movies/${tmdbId}?languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<MovieDetailsResponse>(url);
    }

    public downloadMovie(tmdbId: string, title: string, qualityProfileId: number): Observable<void> {
        const url = `${environment.apiUrl}/discover/movies/${tmdbId}?title=${encodeURIComponent(title)}&qualityProfileId=${qualityProfileId}`;
        return this.http.post<void>(url, null);
    }
    
    public discoverShows(monthsBack: number): Observable<Array<MediaBaseResponse>> {
        const url = `${environment.apiUrl}/discover/shows?monthsBack=${monthsBack}&languageCode=${this.languageService.getCurrentLanguage()}`;
        return this.http.get<Array<MediaBaseResponse>>(url);
    }
}
