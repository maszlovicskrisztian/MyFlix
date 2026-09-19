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
import { MetadataEditor } from '../metadata-editor/metadata-editor';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

const IMDB_ID = /^(?:tt)?(\d{7,8})$/i;

@Component({
  selector: 'app-enrich-dialog',
  imports: [FormsModule, TranslocoModule, MetadataEditor],
  templateUrl: './enrich-dialog.html',
  styleUrl: './enrich-dialog.scss',
})
export class EnrichDialog {
  private metadataService = inject(MetadataService);
  private translocoService = inject(TranslocoService);

  open = model(false);
  mediaId = input<string | null>(null);
  enriched = output<void>();
  busy = signal(false);

  imdbId = linkedSignal<boolean, string>({ source: this.open, computation: () => '' });
  error = linkedSignal<boolean, string | null>({ source: this.open, computation: () => null });

  /** The manual metadata editor stacks on top of this dialog. */
  editorOpen = linkedSignal<boolean, boolean>({ source: this.open, computation: () => false });

  private imdbInput = viewChild<ElementRef<HTMLInputElement>>('imdbInput');

  constructor() {
    effect(() => this.imdbInput()?.nativeElement.focus());
  }

  submit(): void {
    const match = IMDB_ID.exec(this.imdbId().trim());

    if (!match) {
      this.error.set(this.translocoService.translate('ENRICHMENT.ERRORS.INVALID_ID'));
      return;
    }

    const mediaId = this.mediaId();

    if (!mediaId) {
      this.error.set(this.translocoService.translate('ENRICHMENT.ERRORS.MISSING_MEDIA_ID'));
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
        this.error.set(this.translocoService.translate('ENRICHMENT.ERRORS.GENERIC'));
      },
    });
  }

  openEditor(): void {
    if (this.busy()) {
      return;
    }

    this.error.set(null);
    this.editorOpen.set(true);
  }

  onEditorSaved(): void {
    this.open.set(false);
    this.enriched.emit();
  }

  cancel(): void {
    if (this.busy()) {
      return;
    }

    this.open.set(false);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    // The editor is on top, so it handles escape itself.
    if (!this.open() || this.editorOpen()) {
      return;
    }

    this.cancel();
  }
}
