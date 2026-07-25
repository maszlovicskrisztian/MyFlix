import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { ProfileService } from './profile-service';

@Service()
export class AuthService {
    private profileService = inject(ProfileService);
    private http = inject(HttpClient);
    private router = inject(Router);

    private readonly TOKEN_KEY = 'auth_token';

    isAuthenticated = signal<boolean>(!!this.getToken());

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
}
