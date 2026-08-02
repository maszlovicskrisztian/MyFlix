import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';

/** Accepts `tt0111161`, `TT0111161` or a bare `0111161`. */
const IMDB_ID = /^(?:tt)?(\d{7,8})$/i;

@Component({
  selector: 'app-enrich-dialog',
  imports: [FormsModule],
  templateUrl: './enrich-dialog.html',
  styleUrl: './enrich-dialog.scss',
})
export class EnrichDialog implements AfterViewInit {
  busy = input(false);

  confirmed = output<string>();
  cancelled = output<void>();

  imdbId = signal('');
  error = signal<string | null>(null);

  private imdbInput = viewChild<ElementRef<HTMLInputElement>>('imdbInput');

  ngAfterViewInit(): void {
    this.imdbInput()?.nativeElement.focus();
  }

  submit(): void {
    const match = IMDB_ID.exec(this.imdbId().trim());

    if (!match) {
      this.error.set('Érvénytelen azonosító. Például: tt0111161');
      return;
    }

    this.error.set(null);
    this.confirmed.emit(`tt${match[1]}`);
  }

  cancel(): void {
    if (this.busy()) {
      return;
    }

    this.cancelled.emit();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.cancel();
  }
}
