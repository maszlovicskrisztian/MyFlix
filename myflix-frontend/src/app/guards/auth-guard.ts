import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth-service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);

  if (authService.hasValidToken()) {
    return true;
  }

  // Clears the stale token and selected profile, then routes to /login.
  authService.logout();
  return false;
};
