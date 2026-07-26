import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'media',
    },
    {
        path: 'login',
        loadComponent: () => import('./components/login/login').then(m => m.Login),
    },
    { 
        path: 'media', 
        loadComponent: () => import('./components/media-list/media-list').then(m => m.MediaList),
        canActivate: [authGuard]
    },
    { 
        path: 'media/:id', 
        loadComponent: () => import('./components/media-viewer/media-viewer').then(m => m.MediaViewer),
        canActivate: [authGuard]
    },
    { 
        path: 'media/:id/play', 
        loadComponent: () => import('./components/media-player/media-player').then(m => m.MediaPlayer),
        canActivate: [authGuard]
    },
];
