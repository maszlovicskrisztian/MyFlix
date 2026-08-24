import { inject, Service } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

@Service()
export class LanguageService {
    private transloco = inject(TranslocoService);
    private readonly CURRENT_LANGUAGE = 'myflix_selected_language';
    
    setLanguage(languageCode: string): void {
        localStorage.setItem(this.CURRENT_LANGUAGE, languageCode);
        this.transloco.setActiveLang(languageCode);
    }

    getCurrentLanguage(): string {
        const stored = localStorage.getItem(this.CURRENT_LANGUAGE);
        return stored ? stored : this.transloco.getDefaultLang();
    }
}
