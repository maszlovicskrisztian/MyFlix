import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../environments/environment';

@Service()
export class MetadataService {
    http = inject(HttpClient);

    public refreshLibrary() {
            const url = `${environment.apiUrl}/metadata/enrich`;
            return this.http.post(url, {});
        
    }

    public enrichByImdbId(mediaId: string, imdbId: string) {
        const url = `${environment.apiUrl}/metadata/enrich/${mediaId}`;
        return this.http.post<void>(url, { imdbId });
    }
}
