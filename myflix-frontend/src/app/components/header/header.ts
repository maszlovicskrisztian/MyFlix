import { Component, computed, ElementRef, HostListener, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { ProfileService } from '../../services/profile-service';
import { avatarUrl } from '../../model/avatar';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  authService = inject(AuthService);
  profileService = inject(ProfileService);
  router = inject(Router);

  avatarUrl = avatarUrl;

  selectedProfileName = this.profileService.selectedProfileName;
  selectedProfileAvatar = this.profileService.selectedProfileAvatar;
  profileInitial = computed(() => this.selectedProfileName()?.charAt(0) ?? '');
  menuOpen = signal(false);

  private profileMenu = viewChild<ElementRef<HTMLElement>>('profileMenu');

  toggleMenu() {
    this.menuOpen.update((open) => !open);
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
}
