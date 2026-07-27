import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { Profile } from '../model/profile';

@Service()
export class ProfileService {
  private http = inject(HttpClient);
  private readonly PROFILE_KEY = 'myflix_selected_profile';

  selectedProfileId = signal<number | null>(this.loadStoredProfileId());

  private loadStoredProfileId(): number | null {
    const stored = localStorage.getItem(this.PROFILE_KEY);
    return stored ? Number(stored) : null;
  }

  getAllProfiles() {
    return this.http.get<Profile[]>(`${environment.apiUrl}/profiles`);
  }

  createProfile(name: string, avatarKey: string | null) {
    return this.http.post<Profile>(`${environment.apiUrl}/profiles`, { name, avatarKey });
  }

  selectProfile(profileId: number): void {
    localStorage.setItem(this.PROFILE_KEY, String(profileId));
    this.selectedProfileId.set(profileId);
  }

  deleteProfile(profileId: number) {
    return this.http.delete(`${environment.apiUrl}/profiles/${profileId}`);
  }

  clearSelectedProfile(): void {
    localStorage.removeItem(this.PROFILE_KEY);
    this.selectedProfileId.set(null);
  }
}
