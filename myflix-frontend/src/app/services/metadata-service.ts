import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../environments/environment';
import { ManualMetadataRequest } from '../model/manual-metadata-request';
import { MetadataDetails } from '../model/metadata-details';

@Service()
export class MetadataService {
    http = inject(HttpClient);

    public refreshLibrary() {
            const url = `${environment.apiUrl}/metadata/enrich`;
            return this.http.post(url, {});
        
    }

    public enrichByImdbId(mediaId: string, imdbId: string) {
        const url = `${environment.apiUrl}/metadata/enrich/${mediaId}`;
        return this.http.put<void>(url, { imdbId });
    }

    public getMetadata(mediaId: string) {
        const url = `${environment.apiUrl}/metadata/${mediaId}`;
        return this.http.get<MetadataDetails>(url);
    }

    public saveManualMetadata(mediaId: string, request: ManualMetadataRequest) {
        const url = `${environment.apiUrl}/metadata/update/${mediaId}`;
        return this.http.put<void>(url, request);
    }
}
