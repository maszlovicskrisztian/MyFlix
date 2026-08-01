import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard';
import { profileGuard } from './guards/profile-guard';

export const routes: Routes = [
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login',
    },
    {
        path: 'login',
        loadComponent: () => import('./pages/login/login').then(m => m.Login),
    },
    {
        path: 'profiles',
        loadComponent: () => import('./pages/profile-selector/profile-selector').then(m => m.ProfileSelector),
        canActivate: [authGuard],
    },
    {
        path: 'profiles/add',
        loadComponent: () => import('./pages/add-profile/add-profile').then(m => m.AddProfile),
        canActivate: [authGuard],
    },
    {
        path: 'home', 
        loadComponent: () => import('./pages/home/home').then(m => m.Home),
        canActivate: [authGuard, profileGuard]
    },
    {
        path: 'movies', 
        loadComponent: () => import('./pages/movies/movies').then(m => m.Movies),
        canActivate: [authGuard, profileGuard]
    },
    { 
        path: 'media/:id', 
        loadComponent: () => import('./pages/media-viewer/media-viewer').then(m => m.MediaViewer),
        canActivate: [authGuard, profileGuard]
    },
    { 
        path: 'media/:id/play', 
        loadComponent: () => import('./pages/media-player/media-player').then(m => m.MediaPlayer),
        canActivate: [authGuard, profileGuard]
    },
];
