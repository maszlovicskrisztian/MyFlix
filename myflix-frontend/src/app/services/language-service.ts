import { inject, Service } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

@Service()
export class LanguageService {
    private transloco = inject(TranslocoService);
    private readonly CURRENT_LANGUAGE = 'myflix_selected_language';
    
    public setLanguage(languageCode: string): void {
        localStorage.setItem(this.CURRENT_LANGUAGE, languageCode);
        this.transloco.setActiveLang(languageCode);
    }

    public getCurrentLanguage(): string {
        const stored = localStorage.getItem(this.CURRENT_LANGUAGE);
        return stored ? stored : this.transloco.getDefaultLang();
    }

    public changeLanguage(): void {
        const stored = localStorage.getItem(this.CURRENT_LANGUAGE);
        var changed = 'hu';
        if (!stored || stored == 'hu')
            changed = 'en';

        this.setLanguage(changed);
    }
}
