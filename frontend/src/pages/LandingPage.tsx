import { Link } from 'react-router-dom';
import { Shield, TrendingUp, PieChart, Bell, Zap, Lock, ArrowRight, Globe } from 'lucide-react';
import { useI18n } from '../i18n';

export default function LandingPage() {
  const { t, locale, toggleLocale } = useI18n();

  const FEATURES = [
    { icon: TrendingUp, title: t('dcaPlanner'), desc: t('dcaPlannerDesc') },
    { icon: PieChart, title: t('riskAnalyzer'), desc: t('riskAnalyzerDesc') },
    { icon: Bell, title: t('smartAlerts'), desc: t('smartAlertsDesc') },
    { icon: Lock, title: t('privacyFirst'), desc: t('privacyFirstDesc') },
  ];

  return (
    <div className="min-h-screen bg-surface-950 noise-bg">
      <nav className="max-w-7xl mx-auto px-6 py-5 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-brand-500/15 flex items-center justify-center">
            <Shield className="w-4.5 h-4.5 text-brand-400" />
          </div>
          <span className="font-display font-bold text-sm tracking-tight">DCA GUARD</span>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={toggleLocale}
            className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs text-surface-400 hover:text-surface-200 hover:bg-surface-800/50 transition-all"
          >
            <Globe className="w-3.5 h-3.5" />
            <span className="font-mono uppercase">{locale === 'en' ? 'PT' : 'EN'}</span>
          </button>
          <Link to="/app" className="btn-primary text-xs">{t('launchApp')} <ArrowRight className="w-3.5 h-3.5" /></Link>
        </div>
      </nav>

      <section className="max-w-7xl mx-auto px-6 pt-20 pb-32 text-center relative">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-brand-500/5 rounded-full blur-[120px] pointer-events-none" />
        <div className="relative">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-surface-800/80 border border-surface-700 text-xs text-surface-300 mb-6">
            <Zap className="w-3 h-3 text-accent-amber" />
            {t('freeToUse')}
          </div>
          <h1 className="text-4xl sm:text-5xl md:text-6xl font-display font-bold tracking-tight leading-[1.1] mb-6">
            <span className="text-white">{t('heroTitle1')}</span>
            <br />
            <span className="bg-gradient-to-r from-brand-400 to-accent-cyan bg-clip-text text-transparent">{t('heroTitle2')}</span>
          </h1>
          <p className="text-surface-400 text-lg md:text-xl max-w-2xl mx-auto mb-10 leading-relaxed font-body">
            {t('heroDesc')}
          </p>
          <Link to="/app" className="btn-primary text-base px-8 py-3.5 inline-flex items-center gap-2">
            {t('getStartedFree')} <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-6 pb-32">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {FEATURES.map(({ icon: Icon, title, desc }, i) => (
            <div key={title} className="card-hover p-7 group" style={{ animationDelay: `${i * 100}ms` }}>
              <div className="w-10 h-10 rounded-xl bg-brand-500/10 flex items-center justify-center mb-4 group-hover:bg-brand-500/20 transition-colors">
                <Icon className="w-5 h-5 text-brand-400" />
              </div>
              <h3 className="text-base font-semibold text-white mb-2">{title}</h3>
              <p className="text-sm text-surface-400 leading-relaxed">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-6 pb-20">
        <div className="card p-10 text-center glow-green">
          <h2 className="text-2xl font-display font-bold mb-3">{t('readyToGuard')}</h2>
          <p className="text-surface-400 mb-6">{t('readyToGuardDesc')}</p>
          <Link to="/app" className="btn-primary text-base px-8 py-3">
            {t('launchDashboard')} <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </section>

      <footer className="border-t border-surface-800 py-8">
        <div className="max-w-7xl mx-auto px-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-surface-500">
          <div className="flex items-center gap-2">
            <Shield className="w-3.5 h-3.5 text-brand-500/50" />
            <span>{t('footerNote')}</span>
          </div>
          <div className="flex gap-4">
            <span>{t('terms')}</span>
            <span>{t('privacy')}</span>
          </div>
        </div>
      </footer>
    </div>
  );
}