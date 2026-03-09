import React, { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import en, { type TranslationKeys } from './en';
import pt from './pt';

type Locale = 'en' | 'pt';
type Translations = Record<TranslationKeys, string>;

const translations: Record<Locale, Translations> = { en, pt };

interface I18nContextValue {
  locale: Locale;
  t: (key: TranslationKeys, params?: Record<string, string | number>) => string;
  setLocale: (locale: Locale) => void;
  toggleLocale: () => void;
}

const I18nContext = createContext<I18nContextValue>({
  locale: 'en',
  t: (key) => key,
  setLocale: () => {},
  toggleLocale: () => {},
});

function detectLocale(): Locale {
  // Check localStorage first
  const saved = localStorage.getItem('dca-guard-locale');
  if (saved === 'en' || saved === 'pt') return saved;

  // Auto-detect from browser
  const browserLang = navigator.language || (navigator as any).userLanguage || '';
  if (browserLang.startsWith('pt')) return 'pt';

  return 'en';
}

export function I18nProvider({ children }: { children: ReactNode }) {
  const [locale, setLocaleState] = useState<Locale>(detectLocale);

  const setLocale = useCallback((newLocale: Locale) => {
    setLocaleState(newLocale);
    localStorage.setItem('dca-guard-locale', newLocale);
  }, []);

  const toggleLocale = useCallback(() => {
    setLocale(locale === 'en' ? 'pt' : 'en');
  }, [locale, setLocale]);

  const t = useCallback(
    (key: TranslationKeys, params?: Record<string, string | number>): string => {
      let text = translations[locale]?.[key] || translations.en[key] || key;
      if (params) {
        Object.entries(params).forEach(([k, v]) => {
          text = text.replace(`{${k}}`, String(v));
        });
      }
      return text;
    },
    [locale]
  );

  return (
    <I18nContext.Provider value={{ locale, t, setLocale, toggleLocale }}>
      {children}
    </I18nContext.Provider>
  );
}

export const useI18n = () => useContext(I18nContext);