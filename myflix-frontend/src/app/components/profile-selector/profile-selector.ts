import { Component, inject, OnInit, signal } from '@angular/core';
import { ProfileService } from '../../services/profile-service';
import { Router } from '@angular/router';
import { Profile } from '../../model/profile';

@Component({
  selector: 'app-profile-selector',
  imports: [],
  templateUrl: './profile-selector.html',
  styleUrl: './profile-selector.scss',
})
export class ProfileSelector implements OnInit {
  private profileService = inject(ProfileService);
  private router = inject(Router);

  profiles = signal<Profile[]>([]);
  
  ngOnInit(): void {
    this.profileService.getAllProfiles().subscribe({
      next: (profiles) => this.profiles.set(profiles),
    });
  }

  selectProfile(profile: Profile): void {
    this.profileService.selectProfile(profile);
    this.router.navigate(['/media']);
  }

  addProfile(): void {
    const name = prompt('Profil neve:');
    if (!name) return;

    this.profileService.createProfile(name, null).subscribe({
      next: (newProfile) => {
        this.profiles.set([...this.profiles(), newProfile]);
      },
    });
  }

  deleteProfile(profile: Profile): void {
    this.profileService.deleteProfile(profile.id).subscribe({
      next: () => {
        const updatedProfiles = this.profiles().filter(p => p.id !== profile.id);
        this.profiles.set(updatedProfiles);
        if (this.profileService.selectedProfileId() === profile.id) {
          this.profileService.clearSelectedProfile();
        }
      },
    });
  }
}