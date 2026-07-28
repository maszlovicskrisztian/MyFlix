import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { Profile } from '../model/profile';

@Service()
export class ProfileService {
  private http = inject(HttpClient);
  private readonly PROFILE_KEY = 'myflix_selected_profile';
  private readonly PROFILE_NAME_KEY = 'myflix_selected_profile_name';

  selectedProfileId = signal<number | null>(this.loadStoredProfileId());
  selectedProfileName = signal<string | null>(localStorage.getItem(this.PROFILE_NAME_KEY));

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

  selectProfile(profile: Profile): void {
    localStorage.setItem(this.PROFILE_KEY, String(profile.id));
    localStorage.setItem(this.PROFILE_NAME_KEY, profile.name);
    this.selectedProfileId.set(profile.id);
    this.selectedProfileName.set(profile.name);
  }

  deleteProfile(profileId: number) {
    return this.http.delete(`${environment.apiUrl}/profiles/${profileId}`);
  }

  clearSelectedProfile(): void {
    localStorage.removeItem(this.PROFILE_KEY);
    localStorage.removeItem(this.PROFILE_NAME_KEY);
    this.selectedProfileId.set(null);
    this.selectedProfileName.set(null);
  }
}
