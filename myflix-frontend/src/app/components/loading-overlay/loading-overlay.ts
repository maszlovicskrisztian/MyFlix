import { Component, input } from '@angular/core';

/**
 * Full-screen spinner a page can show while its initial data is in flight.
 * Drop it in behind an `@if (loading())` and render the page in the `@else`,
 * so sections never flash their empty state before the response arrives.
 */
@Component({
  selector: 'app-loading-overlay',
  templateUrl: './loading-overlay.html',
  styleUrl: './loading-overlay.scss',
})
export class LoadingOverlay {
  /** Optional caption under the spinner. Nothing is rendered when left empty. */
  label = input('');
}
