import { inject, Service } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

@Service()
export class LanguageService {
    private transloco = inject(TranslocoService);
    
    setLanguage(languageCode: string): void {
        this.transloco.setActiveLang(languageCode);
    }

    getCurrentLanguage(): string {
        return this.transloco.getActiveLang();
    }
}
