import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ProfileService } from '../../services/profile-service';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AVATARS, avatarUrl } from '../../model/avatar';

@Component({
  selector: 'app-add-profile',
  imports: [FormsModule, RouterLink],
  templateUrl: './add-profile.html',
  styleUrl: './add-profile.scss',
})
export class AddProfile {
  profileService = inject(ProfileService);
  router = inject(Router);

  readonly avatars = AVATARS;
  readonly languages = [
    { code: 'en', label: 'English' },
    { code: 'hu', label: 'Magyar' },
  ];

  profileName = signal<string>('');
  preferredLanguage = signal<string>('en');
  selectedIndex = signal<number>(0);
  errorMessage = signal<string>('');

  selectedAvatar = computed(() => this.avatars[this.selectedIndex()]);
  previousAvatar = computed(() => this.avatarAt(this.selectedIndex() - 1));
  nextAvatar = computed(() => this.avatarAt(this.selectedIndex() + 1));

  /** Wraps in both directions so the set can be cycled endlessly. */
  private avatarAt(index: number): string {
    const count = this.avatars.length;
    return this.avatars[((index % count) + count) % count];
  }

  rotate(step: number): void {
    const count = this.avatars.length;
    this.selectedIndex.update((index) => (((index + step) % count) + count) % count);
  }

  avatarSrc = avatarUrl;

  /** "fox-128.ico" -> "fox", used for labels. */
  avatarLabel(avatar: string): string {
    return avatar.split('-')[0];
  }

  addProfile() {
    const name = this.profileName().trim();
    if (!name) {
      this.errorMessage.set('Adjon meg egy profilnevet.');
      return;
    }

    this.errorMessage.set('');

    this.profileService.createProfile(name, this.selectedAvatar(), this.preferredLanguage()).subscribe({
      next: () => this.router.navigate(['/profiles']),
      error: (err: HttpErrorResponse) => {
        console.error('Profil létrehozása sikertelen', err);
        this.errorMessage.set(`A profil mentése nem sikerült (${err.status}). Próbálja újra.`);
      },
    });
  }
}
