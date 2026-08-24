import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  const authService = inject(AuthService);

  if (!authService.hasValidToken()) {
    authService.logout();
    return throwError(() => new HttpErrorResponse({ status: 401 }));
  }

  const token = authService.getToken();

  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};
