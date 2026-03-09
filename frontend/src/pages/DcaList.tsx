import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, TrendingUp, ChevronRight, Calendar, DollarSign } from 'lucide-react';
import { dcaApi, marketApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useI18n } from '../i18n';
import { formatCurrency } from '../utils/format';
import { LoadingSpinner, EmptyState, ErrorMessage } from '../components/common/States';
import AdBanner from '../components/ads/AdBanner';

export default function DcaList() {
  const { user } = useAuth();
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({
    symbol: '', amount: '', frequency: 'WEEKLY', baseCurrency: 'USD', startDate: '',
  });

  const { data: plans, isLoading, error, refetch } = useQuery({
    queryKey: ['dcaPlans'],
    queryFn: dcaApi.list,
    enabled: !!user,
  });

  const { data: assets } = useQuery({
    queryKey: ['assets', ''],
    queryFn: () => marketApi.searchAssets(''),
    enabled: showCreate,
  });

  const createMutation = useMutation({
    mutationFn: dcaApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dcaPlans'] });
      setShowCreate(false);
      setForm({ symbol: '', amount: '', frequency: 'WEEKLY', baseCurrency: 'USD', startDate: '' });
    },
  });

  const handleCreate = () => {
    if (!form.symbol || !form.amount || !form.startDate) return;
    createMutation.mutate({
      symbol: form.symbol,
      amount: parseFloat(form.amount),
      frequency: form.frequency,
      baseCurrency: form.baseCurrency,
      startDate: form.startDate,
    });
  };

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error.message} onRetry={() => refetch()} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-display font-bold">{t('dcaPlansTitle')}</h1>
          <p className="text-sm text-surface-400 mt-1">{t('dcaPlansDesc')}</p>
        </div>
        <button onClick={() => setShowCreate(true)} className="btn-primary text-xs">
          <Plus className="w-4 h-4" /> {t('newPlan')}
        </button>
      </div>

      <AdBanner placement="banner" />

      {plans && plans.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {plans.map(plan => (
            <Link key={plan.id} to={`/app/dca/${plan.id}`} className="card-hover p-5 group">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-accent-cyan/10 flex items-center justify-center">
                    <TrendingUp className="w-5 h-5 text-accent-cyan" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-sm">{plan.assetName} ({plan.symbol})</h3>
                    <p className="text-[10px] text-surface-500">
                      {t('since')} {new Date(plan.startDate).toLocaleDateString()}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`badge ${plan.active ? 'badge-green' : 'badge-red'}`}>
                    {plan.active ? t('active') : t('paused')}
                  </span>
                  <ChevronRight className="w-4 h-4 text-surface-600 group-hover:text-brand-400 transition-colors" />
                </div>
              </div>
              <div className="grid grid-cols-3 gap-3 text-xs">
                <div className="flex items-center gap-1.5">
                  <DollarSign className="w-3 h-3 text-surface-500" />
                  <span className="font-mono font-medium">{formatCurrency(plan.amount)}</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <Calendar className="w-3 h-3 text-surface-500" />
                  <span className="font-medium capitalize">{plan.frequency.toLowerCase()}</span>
                </div>
                <div className="text-surface-400">{plan.executionCount} {t('executions')}</div>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <EmptyState icon={TrendingUp} title={t('noDcaPlansYet')} description={t('noDcaPlansYetDesc')}
          action={<button onClick={() => setShowCreate(true)} className="btn-primary text-xs"><Plus className="w-3.5 h-3.5" /> {t('createPlan')}</button>} />
      )}

      {showCreate && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4" onClick={() => setShowCreate(false)}>
          <div className="card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <h2 className="text-lg font-semibold mb-4">{t('createPlan')}</h2>
            <div className="space-y-4">
              <div>
                <label className="label">{t('asset')}</label>
                <select className="input-field" value={form.symbol} onChange={e => setForm(f => ({ ...f, symbol: e.target.value }))}>
                  <option value="">{t('selectAsset')}</option>
                  {assets?.map(a => (<option key={a.symbol} value={a.symbol}>{a.symbol} — {a.name}</option>))}
                </select>
              </div>
              <div>
                <label className="label">{t('amountPerBuy')}</label>
                <input type="number" step="1" placeholder="100" className="input-field" value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} />
              </div>
              <div>
                <label className="label">{t('frequency')}</label>
                <select className="input-field" value={form.frequency} onChange={e => setForm(f => ({ ...f, frequency: e.target.value }))}>
                  <option value="DAILY">{t('daily')}</option>
                  <option value="WEEKLY">{t('weekly')}</option>
                  <option value="BIWEEKLY">{t('biweekly')}</option>
                  <option value="MONTHLY">{t('monthly')}</option>
                </select>
              </div>
              <div>
                <label className="label">{t('currency')}</label>
                <select className="input-field" value={form.baseCurrency} onChange={e => setForm(f => ({ ...f, baseCurrency: e.target.value }))}>
                  <option value="USD">USD</option>
                  <option value="BRL">BRL</option>
                </select>
              </div>
              <div>
                <label className="label">{t('startDate')}</label>
                <input type="date" className="input-field" value={form.startDate} onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))} />
              </div>
              {createMutation.error && <p className="text-xs text-accent-rose">{createMutation.error.message}</p>}
              <div className="flex gap-3">
                <button onClick={() => setShowCreate(false)} className="btn-secondary flex-1">{t('cancel')}</button>
                <button onClick={handleCreate} disabled={createMutation.isPending || !form.symbol || !form.amount || !form.startDate} className="btn-primary flex-1">
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