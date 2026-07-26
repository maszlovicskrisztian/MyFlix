import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'media',
    },
    { 
        path: 'media', 
        loadComponent: () => import('./components/media-list/media-list').then(m => m.MediaList)
    },
    { 
        path: 'media/:id', 
        loadComponent: () => import('./components/media-viewer/media-viewer').then(m => m.MediaViewer)
    },
    { 
        path: 'media/:id/play', 
        loadComponent: () => import('./components/media-player/media-player').then(m => m.MediaPlayer)
    },
];
