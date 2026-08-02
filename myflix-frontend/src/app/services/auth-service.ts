import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../environments/environment';
import { ProfileService } from './profile-service';

@Service()
export class AuthService {
    private profileService = inject(ProfileService);
    private http = inject(HttpClient);
    private router = inject(Router);

    private readonly TOKEN_KEY = 'auth_token';

    isAuthenticated = signal<boolean>(this.hasValidToken());

    login(username: string, password: string) {
        const url = `${environment.apiUrl}/auth/login`;
        return this.http.post<{ token: string }>(url, { username, password });
    }

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        this.isAuthenticated.set(false);
        this.profileService.clearSelectedProfile();
        this.router.navigate(['/login']);
    }

    setToken(token: string) {
        localStorage.setItem(this.TOKEN_KEY, token);
        this.isAuthenticated.set(true);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    hasValidToken(): boolean {
        const token = this.getToken();
        return !!token && !this.isExpired(token);
    }

    private isExpired(token: string): boolean {
        const exp = this.readExpiry(token);

        return exp !== null && exp <= Date.now();
    }

    private readExpiry(token: string): number | null {
        const segments = token.split('.');
        if (segments.length !== 3) {
            return null;
        }

        try {
            const base64 = segments[1].replace(/-/g, '+').replace(/_/g, '/');
            const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
            const payload = JSON.parse(atob(padded)) as { exp?: unknown };

            return typeof payload.exp === 'number' ? payload.exp * 1000 : null;
        } catch {
            return null;
        }
    }
}
