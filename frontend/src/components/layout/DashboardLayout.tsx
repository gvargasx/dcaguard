import { Outlet, NavLink } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useI18n } from '../../i18n';
import { Shield, LayoutDashboard, Briefcase, TrendingUp, Menu, X, LogOut, Globe } from 'lucide-react';
import { cn } from '../../utils/format';
import AdBanner from '../ads/AdBanner';

export default function DashboardLayout() {
  const { user, logout } = useAuth();
  const { t, locale, toggleLocale } = useI18n();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const NAV_ITEMS = [
    { to: '/app', icon: LayoutDashboard, label: t('dashboard'), end: true },
    { to: '/app/portfolio', icon: Briefcase, label: t('portfolio'), end: false },
    { to: '/app/dca', icon: TrendingUp, label: t('dcaPlans'), end: false },
  ];

  return (
    <div className="min-h-screen bg-surface-950 noise-bg flex">
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/60 z-40 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      <aside
        className={cn(
          'fixed lg:sticky top-0 left-0 z-50 h-screen w-64 bg-surface-900/95 backdrop-blur-xl border-r border-surface-800 flex flex-col transition-transform duration-300',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
      >
        <div className="p-5 flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-brand-500/15 flex items-center justify-center">
            <Shield className="w-5 h-5 text-brand-400" />
          </div>
          <div>
            <h1 className="font-display font-bold text-sm tracking-tight">DCA GUARD</h1>
            <p className="text-[10px] text-surface-500 uppercase tracking-widest">Risk · Plan · Grow</p>
          </div>
          <button onClick={() => setSidebarOpen(false)} className="lg:hidden ml-auto text-surface-400">
            <X className="w-5 h-5" />
          </button>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1">
          {NAV_ITEMS.map(({ to, icon: Icon, label, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200',
                  isActive
                    ? 'bg-brand-500/10 text-brand-400 shadow-sm'
                    : 'text-surface-400 hover:bg-surface-800 hover:text-surface-200'
                )
              }
            >
              <Icon className="w-[18px] h-[18px]" />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Sidebar Ad */}
        <div className="px-3 pb-3">
          <AdBanner placement="sidebar" />
        </div>

        {/* Language Toggle */}
        <div className="px-3 pb-2">
          <button
            onClick={toggleLocale}
            className="flex items-center gap-2 w-full px-3 py-2 rounded-xl text-xs text-surface-400 hover:bg-surface-800 hover:text-surface-200 transition-all"
          >
            <Globe className="w-4 h-4" />
            <span>{locale === 'en' ? 'Português' : 'English'}</span>
            <span className="ml-auto text-[10px] font-mono text-surface-600 uppercase">{locale}</span>
          </button>
        </div>

        {/* User */}
        <div className="p-4 border-t border-surface-800">
          {user ? (
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-surface-700 flex items-center justify-center text-xs font-bold text-surface-300">
                {user.displayName?.charAt(0) || user.email?.charAt(0) || '?'}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs font-medium text-surface-200 truncate">{user.displayName || user.email || 'Anonymous'}</p>
                <p className="text-[10px] text-surface-500">{user.planType} {t('plan')}</p>
              </div>
              <button
                onClick={() => logout()}
                className="p-1.5 rounded-lg hover:bg-surface-700 text-surface-500 hover:text-accent-rose transition-colors"
                title="Sign out"
              >
                <LogOut className="w-3.5 h-3.5" />
              </button>
            </div>
          ) : (
            <p className="text-xs text-surface-500 text-center">{t('notSignedIn')}</p>
          )}
        </div>
      </aside>

      <div className="flex-1 min-w-0">
        <header className="lg:hidden sticky top-0 z-30 bg-surface-950/90 backdrop-blur-xl border-b border-surface-800 px-4 py-3 flex items-center gap-3">
          <button onClick={() => setSidebarOpen(true)} className="text-surface-300 hover:text-white">
            <Menu className="w-5 h-5" />
          </button>
          <div className="flex items-center gap-2">
            <Shield className="w-4 h-4 text-brand-400" />
            <span className="font-display font-bold text-xs">DCA GUARD</span>
          </div>
          <button
            onClick={toggleLocale}
            className="ml-auto p-1.5 rounded-lg text-surface-400 hover:text-surface-200 hover:bg-surface-800 transition-all"
          >
            <Globe className="w-4 h-4" />
          </button>
        </header>

        <main className="page-container animate-fade-in">
          <Outlet />
        </main>
      </div>
    </div>
  );
}