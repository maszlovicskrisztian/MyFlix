import { Routes } from '@angular/router';
import { MediaList } from './components/media-list/media-list';

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

];
