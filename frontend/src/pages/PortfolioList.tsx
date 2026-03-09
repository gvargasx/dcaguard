import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Briefcase, ChevronRight } from 'lucide-react';
import { portfolioApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useI18n } from '../i18n';
import { LoadingSpinner, EmptyState, ErrorMessage } from '../components/common/States';
import AdBanner from '../components/ads/AdBanner';

export default function PortfolioList() {
  const { user } = useAuth();
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ name: '', baseCurrency: 'USD' });

  const { data: portfolios, isLoading, error, refetch } = useQuery({
    queryKey: ['portfolios'],
    queryFn: portfolioApi.list,
    enabled: !!user,
  });

  const createMutation = useMutation({
    mutationFn: portfolioApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios'] });
      setShowCreate(false);
      setForm({ name: '', baseCurrency: 'USD' });
    },
  });

  const handleCreate = () => {
    if (!form.name.trim()) return;
    createMutation.mutate(form);
  };

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error.message} onRetry={() => refetch()} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-display font-bold">{t('portfolios')}</h1>
          <p className="text-sm text-surface-400 mt-1">{t('portfoliosDesc')}</p>
        </div>
        <button onClick={() => setShowCreate(true)} className="btn-primary text-xs">
          <Plus className="w-4 h-4" /> {t('newPortfolio')}
        </button>
      </div>

      <AdBanner placement="banner" />

      {portfolios && portfolios.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {portfolios.map(portfolio => (
            <Link key={portfolio.id} to={`/app/portfolio/${portfolio.id}`} className="card-hover p-5 group">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-brand-500/10 flex items-center justify-center">
                    <Briefcase className="w-5 h-5 text-brand-400" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-sm">{portfolio.name}</h3>
                    <p className="text-[10px] text-surface-500">
                      {portfolio.positionCount} {t('positions').toLowerCase()} · {portfolio.baseCurrency}
                    </p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-surface-600 group-hover:text-brand-400 transition-colors" />
              </div>
              <p className="text-xs text-surface-500">
                {t('created')} {new Date(portfolio.createdAt).toLocaleDateString()}
              </p>
            </Link>
          ))}
        </div>
      ) : (
        <EmptyState icon={Briefcase} title={t('noPortfoliosYet')} description={t('noPortfoliosDesc')}
          action={<button onClick={() => setShowCreate(true)} className="btn-primary text-xs"><Plus className="w-3.5 h-3.5" /> {t('createPortfolio')}</button>} />
      )}

      {showCreate && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4" onClick={() => setShowCreate(false)}>
          <div className="card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <h2 className="text-lg font-semibold mb-4">{t('createPortfolio')}</h2>
            <div className="space-y-4">
              <div>
                <label className="label">{t('portfolioName')}</label>
                <input type="text" placeholder="My Portfolio" className="input-field" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
              </div>
              <div>
                <label className="label">{t('baseCurrency')}</label>
                <select className="input-field" value={form.baseCurrency} onChange={e => setForm(f => ({ ...f, baseCurrency: e.target.value }))}>
                  <option value="USD">USD</option>
                  <option value="BRL">BRL</option>
                  <option value="EUR">EUR</option>
                </select>
              </div>
              {createMutation.error && <p className="text-xs text-accent-rose">{createMutation.error.message}</p>}
              <div className="flex gap-3">
                <button onClick={() => setShowCreate(false)} className="btn-secondary flex-1">{t('cancel')}</button>
                <button onClick={handleCreate} disabled={createMutation.isPending || !form.name.trim()} className="btn-primary flex-1">
                  {createMutation.isPending ? t('creating') : t('create')}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}