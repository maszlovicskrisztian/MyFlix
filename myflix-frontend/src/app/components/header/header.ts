import { Component, computed, ElementRef, HostListener, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { ProfileService } from '../../services/profile-service';
import { avatarUrl } from '../../model/avatar';
import { MetadataService } from '../../services/metadata-service';
import { MediaSearchDialog } from '../media-search-dialog/media-search-dialog';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, MediaSearchDialog],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  authService = inject(AuthService);
  profileService = inject(ProfileService);
  metadataService = inject(MetadataService);
  router = inject(Router);

  avatarUrl = avatarUrl;

  selectedProfileName = this.profileService.selectedProfileName;
  selectedProfileAvatar = this.profileService.selectedProfileAvatar;
  profileInitial = computed(() => this.selectedProfileName()?.charAt(0) ?? '');
  menuOpen = signal(false);
  searchOpen = signal(false);

  private profileMenu = viewChild<ElementRef<HTMLElement>>('profileMenu');

  toggleMenu() {
    this.menuOpen.update((open) => !open);
  }

  openSearch() {
    this.menuOpen.set(false);
    this.searchOpen.set(true);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.menuOpen()) return;

    const menu = this.profileMenu()?.nativeElement;
    if (menu && !menu.contains(event.target as Node)) {
      this.menuOpen.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    this.menuOpen.set(false);
  }

  switchProfile() {
    this.menuOpen.set(false);
    this.router.navigate(['/profiles']);
  }

  logout() {
    this.menuOpen.set(false);
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  refreshLibrary() {
    this.menuOpen.set(false);
    this.metadataService.refreshLibrary().subscribe({
        next: () => console.log('Dúsítás elindítva'),
        error: (err) => console.error('Hiba', err)
    });
  }
}
