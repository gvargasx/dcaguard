import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Briefcase, TrendingUp, Shield, ArrowUpRight, ArrowDownRight, Plus, LogIn, UserPlus,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useI18n } from '../i18n';
import { portfolioApi, dcaApi } from '../services/api';
import { formatCurrency, formatPercent, getRiskColor } from '../utils/format';
import { LoadingSpinner, EmptyState } from '../components/common/States';
import AdBanner from '../components/ads/AdBanner';

export default function Dashboard() {
  const { user, loading: authLoading, error: authError, loginWithGoogle, loginAnonymously } = useAuth();
  const { t } = useI18n();

  const { data: portfolios, isLoading: loadingP } = useQuery({
    queryKey: ['portfolios'],
    queryFn: portfolioApi.list,
    enabled: !!user,
  });

  const { data: dcaPlans, isLoading: loadingD } = useQuery({
    queryKey: ['dcaPlans'],
    queryFn: dcaApi.list,
    enabled: !!user,
  });

  const firstPortfolioId = portfolios?.[0]?.id;

  const { data: analysis } = useQuery({
    queryKey: ['portfolioAnalysis', firstPortfolioId],
    queryFn: () => portfolioApi.analyze(firstPortfolioId!),
    enabled: !!firstPortfolioId,
  });

  const handleGoogleLogin = async () => {
    try { await loginWithGoogle(); } catch (err) { console.error(err); }
  };

  const handleAnonymousLogin = async () => {
    try { await loginAnonymously(); } catch (err) { console.error(err); }
  };

  if (authLoading) return <LoadingSpinner message={t('checkingAuth')} />;

  if (!user) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="w-16 h-16 rounded-2xl bg-brand-500/10 flex items-center justify-center mb-6">
          <Shield className="w-8 h-8 text-brand-400" />
        </div>
        <h2 className="text-xl font-display font-bold mb-2">{t('welcomeTo')}</h2>
        <p className="text-sm text-surface-400 max-w-md mb-8">{t('authDesc')}</p>
        {authError && (
          <div className="max-w-sm w-full mb-4 p-3 rounded-xl bg-accent-rose/10 border border-accent-rose/30 text-xs text-accent-rose">
            {authError}
          </div>
        )}
        <div className="flex flex-col sm:flex-row gap-3">
          <button onClick={handleGoogleLogin} disabled={authLoading} className="btn-primary px-6 py-3">
            <LogIn className="w-4 h-4" /> {t('signInGoogle')}
          </button>
          <button onClick={handleAnonymousLogin} disabled={authLoading} className="btn-secondary px-6 py-3">
            <UserPlus className="w-4 h-4" /> {t('tryAnonymously')}
          </button>
        </div>
        <p className="text-[11px] text-surface-500 mt-6 max-w-sm">{t('anonymousNote')}</p>
      </div>
    );
  }

  if (loadingP || loadingD) return <LoadingSpinner />;

  const hasPortfolios = (portfolios?.length ?? 0) > 0;
  const hasDcaPlans = (dcaPlans?.length ?? 0) > 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-display font-bold">
          {t('welcome')}{user.displayName ? `, ${user.displayName.split(' ')[0]}` : ''}
        </h1>
        <p className="text-sm text-surface-400 mt-1">{t('portfolioOverview')}</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label={t('portfolioValue')} value={analysis ? formatCurrency(analysis.totalValue) : '—'} change={analysis ? formatPercent(analysis.totalPnlPercent) : undefined} positive={analysis ? analysis.totalPnlPercent >= 0 : undefined} />
        <StatCard label={t('totalPnl')} value={analysis ? formatCurrency(analysis.totalPnl) : '—'} change={analysis ? formatPercent(analysis.totalPnlPercent) : undefined} positive={analysis ? analysis.totalPnl >= 0 : undefined} />
        <StatCard label={t('riskScore')} value={analysis ? `${analysis.riskScore}/100` : '—'} subtitle={analysis?.riskLevel} riskScore={analysis?.riskScore} />
        <StatCard label={t('activeDcaPlans')} value={`${dcaPlans?.filter((p) => p.active).length ?? 0}`} subtitle={t('ofMax', { max: user.maxDcaPlans })} />
      </div>

      <AdBanner placement="banner" />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold flex items-center gap-2">
              <Briefcase className="w-4 h-4 text-brand-400" /> {t('positions')}
            </h2>
            {hasPortfolios && (
              <Link to={`/app/portfolio/${portfolios![0].id}`} className="text-xs text-brand-400 hover:underline">{t('viewAll')}</Link>
            )}
          </div>
          {analysis && analysis.positions.length > 0 ? (
            <div className="space-y-3">
              {analysis.positions.slice(0, 5).map((pos) => (
                <div key={pos.id} className="flex items-center justify-between py-2 border-b border-surface-700/50 last:border-0">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-lg bg-surface-700 flex items-center justify-center text-xs font-bold font-mono text-surface-300">{pos.symbol.slice(0, 4)}</div>
                    <div>
                      <p className="text-sm font-medium">{pos.symbol}</p>
                      <p className="text-[10px] text-surface-500">{pos.allocationPercent?.toFixed(1)}%</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-mono">{formatCurrency(pos.currentValue)}</p>
                    <p className={`text-[10px] font-mono ${pos.pnl !== null && pos.pnl >= 0 ? 'text-brand-400' : 'text-accent-rose'}`}>
                      {pos.pnlPercent !== null ? formatPercent(pos.pnlPercent) : '—'}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState icon={Briefcase} title={t('noPositionsYet')} description={t('noPositionsDesc')}
              action={<Link to="/app/portfolio" className="btn-primary text-xs"><Plus className="w-3.5 h-3.5" /> {t('createPortfolio')}</Link>} />
          )}
        </div>

        <div className="card p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-accent-cyan" /> {t('dcaPlans')}
            </h2>
            <Link to="/app/dca" className="text-xs text-brand-400 hover:underline">{t('manage')}</Link>
          </div>
          {hasDcaPlans ? (
            <div className="space-y-3">
              {dcaPlans!.map((plan) => (
                <Link key={plan.id} to={`/app/dca/${plan.id}`}
                  className="block p-3 rounded-xl bg-surface-800/40 border border-surface-700/30 hover:border-brand-500/30 transition-all">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-mono font-bold text-sm">{plan.symbol}</span>
                    <span className="badge-green text-[10px]">{plan.frequency.toLowerCase()}</span>
                  </div>
                  <p className="text-xs text-surface-400">
                    {formatCurrency(plan.amount)} / {plan.frequency.toLowerCase()} · {plan.executionCount} {t('executions')}
                  </p>
                </Link>
              ))}
            </div>
          ) : (
            <EmptyState icon={TrendingUp} title={t('noDcaPlans')} description={t('noDcaPlansDesc')}
              action={<Link to="/app/dca" className="btn-primary text-xs"><Plus className="w-3.5 h-3.5" /> {t('createPlan')}</Link>} />
          )}
        </div>
      </div>

      <div className="flex items-start gap-3 p-4 rounded-xl bg-surface-800/30 border border-surface-700/30">
        <Shield className="w-4 h-4 text-surface-500 mt-0.5 flex-shrink-0" />
        <p className="text-[11px] text-surface-500 leading-relaxed">{t('disclaimer')}</p>
      </div>
    </div>
  );
}

function StatCard({ label, value, change, subtitle, positive, riskScore }: {
  label: string; value: string; change?: string; subtitle?: string; positive?: boolean; riskScore?: number;
}) {
  return (
    <div className="card p-4 animate-slide-up">
      <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-2">{label}</p>
      <p className={`stat-value text-xl ${riskScore !== undefined ? getRiskColor(riskScore) : 'text-white'}`}>{value}</p>
      {change && (
        <div className={`flex items-center gap-1 mt-1 text-xs font-mono ${positive ? 'text-brand-400' : 'text-accent-rose'}`}>
          {positive ? <ArrowUpRight className="w-3 h-3" /> : <ArrowDownRight className="w-3 h-3" />}
          {change}
        </div>
      )}
      {subtitle && <p className={`text-xs mt-1 ${riskScore !== undefined ? getRiskColor(riskScore) : 'text-surface-400'}`}>{subtitle}</p>}
    </div>
  );
}