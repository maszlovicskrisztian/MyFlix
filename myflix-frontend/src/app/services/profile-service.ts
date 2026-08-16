import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { environment } from '../environments/environment';
import { Profile } from '../model/profile';
import { Observable } from 'rxjs';
import { LanguageService } from './language-service';

@Service()
export class ProfileService {
  private http = inject(HttpClient);
  private languageService = inject(LanguageService);
  private readonly PROFILE_KEY = 'myflix_selected_profile';
  private readonly PROFILE_NAME_KEY = 'myflix_selected_profile_name';
  private readonly PROFILE_AVATAR_KEY = 'myflix_selected_profile_avatar';

  selectedProfileId = signal<number | null>(this.loadStoredProfileId());
  selectedProfileName = signal<string | null>(localStorage.getItem(this.PROFILE_NAME_KEY));
  selectedProfileAvatar = signal<string | null>(localStorage.getItem(this.PROFILE_AVATAR_KEY));

  private loadStoredProfileId(): number | null {
    const stored = localStorage.getItem(this.PROFILE_KEY);
    return stored ? Number(stored) : null;
  }

  getAllProfiles(): Observable<Array<Profile>> {
    return this.http.get<Array<Profile>>(`${environment.apiUrl}/profiles`);
  }

  createProfile(name: string, avatarKey: string | null, preferredLanguage: string) {
    return this.http.post<Profile>(`${environment.apiUrl}/profiles`, {
      name,
      avatarKey,
      preferredLanguage,
    });
  }

  selectProfile(profile: Profile): void {
    localStorage.setItem(this.PROFILE_KEY, String(profile.id));
    localStorage.setItem(this.PROFILE_NAME_KEY, profile.name);
    this.selectedProfileId.set(profile.id);
    this.selectedProfileName.set(profile.name);
    this.languageService.setLanguage(profile.preferredLanguage);

    // Profiles created before avatars existed have none — fall back to the initial.
    if (profile.avatarKey) {
      localStorage.setItem(this.PROFILE_AVATAR_KEY, profile.avatarKey);
    } else {
      localStorage.removeItem(this.PROFILE_AVATAR_KEY);
    }
    this.selectedProfileAvatar.set(profile.avatarKey || null);
  }

  deleteProfile(profileId: number) {
    return this.http.delete(`${environment.apiUrl}/profiles/${profileId}`);
  }

  clearSelectedProfile(): void {
    localStorage.removeItem(this.PROFILE_KEY);
    localStorage.removeItem(this.PROFILE_NAME_KEY);
    localStorage.removeItem(this.PROFILE_AVATAR_KEY);
    this.selectedProfileId.set(null);
    this.selectedProfileName.set(null);
    this.selectedProfileAvatar.set(null);
  }
}
