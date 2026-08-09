import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // A 401 from /auth/login means bad credentials — the login form reports that
      // itself. Anywhere else it means the session expired, so drop it.
      if (error.status === 401 && !req.url.includes('/auth/')) {
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};
