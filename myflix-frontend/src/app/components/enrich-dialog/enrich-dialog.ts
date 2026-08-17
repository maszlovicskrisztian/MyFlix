import {
  Component,
  ElementRef,
  HostListener,
  effect,
  inject,
  input,
  linkedSignal,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MetadataService } from '../../services/metadata-service';
import { TranslocoModule } from '@jsverse/transloco';

/** Accepts `tt0111161`, `TT0111161` or a bare `0111161`. */
const IMDB_ID = /^(?:tt)?(\d{7,8})$/i;

@Component({
  selector: 'app-enrich-dialog',
  imports: [FormsModule, TranslocoModule],
  templateUrl: './enrich-dialog.html',
  styleUrl: './enrich-dialog.scss',
})
export class EnrichDialog {
  private metadataService = inject(MetadataService);

  open = model(false);
  mediaId = input<string | null>(null);
  enriched = output<void>();
  busy = signal(false);

  imdbId = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  error = linkedSignal<boolean, string | null>({ source: this.open, computation: () => null });

  private imdbInput = viewChild<ElementRef<HTMLInputElement>>('imdbInput');

  constructor() {
    effect(() => this.imdbInput()?.nativeElement.focus());
  }

  submit(): void {
    const match = IMDB_ID.exec(this.imdbId().trim());

    if (!match) {
      this.error.set('Érvénytelen azonosító. Például: tt0111161');
      return;
    }

    const mediaId = this.mediaId();

    if (!mediaId) {
      this.error.set('Hiányzik a tartalom azonosítója.');
      return;
    }

    this.error.set(null);
    this.busy.set(true);

    this.metadataService.enrichByImdbId(mediaId, `tt${match[1]}`).subscribe({
      next: () => {
        this.busy.set(false);
        this.open.set(false);
        this.enriched.emit();
      },
      error: (error) => {
        console.error('Error enriching metadata:', error);
        this.busy.set(false);
        this.error.set('Nem sikerült betölteni a metaadatokat.');
      },
    });
  }

  cancel(): void {
    if (this.busy()) {
      return;
    }

    this.open.set(false);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (!this.open()) {
      return;
    }

    this.cancel();
  }
}
