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
        loadComponent: () => import('./components/login/login').then(m => m.Login),
    },
    {
        path: 'profiles',
        loadComponent: () => import('./components/profile-selector/profile-selector').then(m => m.ProfileSelector),
        canActivate: [authGuard],
    },
    {
        path: 'profiles/add',
        loadComponent: () => import('./components/add-profile/add-profile').then(m => m.AddProfile),
        canActivate: [authGuard],
    },
    { 
        path: 'media', 
        loadComponent: () => import('./components/media-list/media-list').then(m => m.MediaList),
        canActivate: [authGuard, profileGuard]
    },
    { 
        path: 'media/:id', 
        loadComponent: () => import('./components/media-viewer/media-viewer').then(m => m.MediaViewer),
        canActivate: [authGuard, profileGuard]
    },
    { 
        path: 'media/:id/play', 
        loadComponent: () => import('./components/media-player/media-player').then(m => m.MediaPlayer),
        canActivate: [authGuard, profileGuard]
    },
];
