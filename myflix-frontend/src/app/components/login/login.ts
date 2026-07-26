import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = signal<string>('');
  password = signal<string>('');
  errorMessage = signal<string>('');

  login() {
    this.authService.login(this.username(), this.password()).subscribe({
      next: (response) => {
        this.authService.setToken(response.token);
        this.router.navigate(['/media']);
      },
      error: () => {
        this.errorMessage.set('Invalid username or password');
      }
    });
  }
}
