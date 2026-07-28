import { Component, computed, ElementRef, HostListener, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { ProfileService } from '../../services/profile-service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  authService = inject(AuthService);
  router = inject(Router);
  private readonly PROFILE_NAME_KEY = 'myflix_selected_profile_name';
  
  selectedProfileName = signal<string | null>(localStorage.getItem(this.PROFILE_NAME_KEY));
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
