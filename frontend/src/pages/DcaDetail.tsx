import { useParams, Link } from 'react-router-dom';
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Plus, Bell, TrendingDown, TrendingUp } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, LineChart, Line } from 'recharts';
import { dcaApi } from '../services/api';
import { formatCurrency, formatPercent, getPnlColor, getSeverityColor } from '../utils/format';
import { LoadingSpinner, EmptyState, ErrorMessage } from '../components/common/States';
import AdBanner from '../components/ads/AdBanner';

export default function DcaDetail() {
  const { id } = useParams();
  const planId = Number(id);
  const queryClient = useQueryClient();
  const [showExec, setShowExec] = useState(false);
  const [tab, setTab] = useState<'chart' | 'executions'>('chart');
  const [execForm, setExecForm] = useState({ executionDate: '', amountPaid: '', priceAtExec: '' });

  const { data: plan, isLoading: loadingPlan } = useQuery({
    queryKey: ['dcaPlan', planId],
    queryFn: () => dcaApi.get(planId),
  });

  const { data: summary, isLoading: loadingSummary, error, refetch } = useQuery({
    queryKey: ['dcaSummary', planId],
    queryFn: () => dcaApi.getSummary(planId),
  });

  const { data: alerts } = useQuery({
    queryKey: ['dcaAlerts', planId],
    queryFn: () => dcaApi.getAlerts(planId),
  });

  const addExecMutation = useMutation({
    mutationFn: (data: { executionDate: string; amountPaid: number; priceAtExec: number }) =>
      dcaApi.addExecution(planId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dcaSummary', planId] });
      queryClient.invalidateQueries({ queryKey: ['dcaAlerts', planId] });
      queryClient.invalidateQueries({ queryKey: ['dcaPlan', planId] });
      setShowExec(false);
      setExecForm({ executionDate: '', amountPaid: '', priceAtExec: '' });
    },
  });

  const handleAddExec = () => {
    if (!execForm.executionDate || !execForm.amountPaid || !execForm.priceAtExec) return;
    addExecMutation.mutate({
      executionDate: execForm.executionDate,
      amountPaid: parseFloat(execForm.amountPaid),
      priceAtExec: parseFloat(execForm.priceAtExec),
    });
  };

  if (loadingPlan || loadingSummary) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error.message} onRetry={() => refetch()} />;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Link to="/app/dca" className="btn-ghost p-2"><ArrowLeft className="w-4 h-4" /></Link>
        <div className="flex-1">
          <h1 className="text-xl font-display font-bold">{plan?.symbol || ''} DCA Plan</h1>
          <p className="text-xs text-surface-500">
            {plan ? `${formatCurrency(plan.amount)} ${plan.frequency.toLowerCase()} since ${plan.startDate}` : ''}
          </p>
        </div>
        <button onClick={() => setShowExec(true)} className="btn-primary text-xs">
          <Plus className="w-4 h-4" /> Log Execution
        </button>
      </div>

      {/* Stats */}
      {summary ? (
        <>
          <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
            <div className="card p-4">
              <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Total Invested</p>
              <p className="text-lg font-display font-bold">{formatCurrency(summary.totalInvested)}</p>
            </div>
            <div className="card p-4">
              <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Current Value</p>
              <p className="text-lg font-display font-bold">{formatCurrency(summary.currentValue)}</p>
            </div>
            <div className="card p-4">
              <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">PnL</p>
              <p className={`text-lg font-display font-bold ${getPnlColor(summary.pnl)}`}>{formatCurrency(summary.pnl)}</p>
              <p className={`text-xs font-mono ${getPnlColor(summary.pnlPercent)}`}>{formatPercent(summary.pnlPercent)}</p>
            </div>
            <div className="card p-4">
              <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Avg Price</p>
              <p className="text-lg font-display font-bold">{formatCurrency(summary.averagePrice)}</p>
            </div>
            <div className="card p-4">
              <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Executions</p>
              <p className="text-lg font-display font-bold">{summary.totalExecutions}</p>
            </div>
          </div>

          {/* Alerts */}
          {alerts && alerts.length > 0 && (
            <div className="card p-5">
              <h2 className="text-sm font-semibold flex items-center gap-2 mb-3">
                <Bell className="w-4 h-4 text-accent-amber" /> Alerts
              </h2>
              <div className="space-y-2">
                {alerts.map((alert, i) => (
                  <div key={i} className="flex items-start gap-3 p-3 rounded-xl bg-surface-800/50 border border-surface-700/30">
                    {alert.type === 'DIP_ALERT' ? (
                      <TrendingDown className="w-4 h-4 text-accent-amber mt-0.5 flex-shrink-0" />
                    ) : (
                      <TrendingUp className="w-4 h-4 text-accent-cyan mt-0.5 flex-shrink-0" />
                    )}
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className={`text-xs font-semibold ${getSeverityColor(alert.severity)}`}>
                          {alert.type.replace('_', ' ')}
                        </span>
                        <span className={`badge ${alert.severity === 'HIGH' ? 'badge-red' : alert.severity === 'MEDIUM' ? 'badge-amber' : 'badge-cyan'}`}>
                          {alert.severity}
                        </span>
                      </div>
                      <p className="text-xs text-surface-300 leading-relaxed">{alert.message}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <AdBanner placement="inline" />

          {/* Chart / Executions Tab */}
          <div className="card overflow-hidden">
            <div className="flex border-b border-surface-700">
              <button onClick={() => setTab('chart')} className={`px-5 py-3 text-sm font-medium transition-colors ${tab === 'chart' ? 'text-brand-400 border-b-2 border-brand-400' : 'text-surface-400 hover:text-surface-200'}`}>
                Performance Chart
              </button>
              <button onClick={() => setTab('executions')} className={`px-5 py-3 text-sm font-medium transition-colors ${tab === 'executions' ? 'text-brand-400 border-b-2 border-brand-400' : 'text-surface-400 hover:text-surface-200'}`}>
                Executions ({summary.executions.length})
              </button>
            </div>

            {tab === 'chart' ? (
              <div className="p-5">
                {summary.chartData.length > 0 ? (
                  <>
                    <div className="h-72 md:h-96">
                      <ResponsiveContainer width="100%" height="100%">
                        <AreaChart data={summary.chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                          <defs>
                            <linearGradient id="investedGrad" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="5%" stopColor="#64748b" stopOpacity={0.3} />
                              <stop offset="95%" stopColor="#64748b" stopOpacity={0} />
                            </linearGradient>
                            <linearGradient id="valueGrad" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="5%" stopColor="#22c55e" stopOpacity={0.3} />
                              <stop offset="95%" stopColor="#22c55e" stopOpacity={0} />
                            </linearGradient>
                          </defs>
                          <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                          <XAxis dataKey="date" tick={{ fill: '#64748b', fontSize: 10 }} tickFormatter={d => new Date(d).toLocaleDateString('en', { month: 'short', day: 'numeric' })} interval="preserveStartEnd" />
                          <YAxis tick={{ fill: '#64748b', fontSize: 10 }} tickFormatter={v => `$${(v / 1000).toFixed(1)}k`} />
                          <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', fontSize: '11px' }} formatter={(val: number, n: string) => [formatCurrency(val), n]} />
                          <Area type="monotone" dataKey="totalInvested" name="Total Invested" stroke="#64748b" fill="url(#investedGrad)" strokeWidth={1.5} />
                          <Area type="monotone" dataKey="portfolioValue" name="Portfolio Value" stroke="#22c55e" fill="url(#valueGrad)" strokeWidth={2} />
                        </AreaChart>
                      </ResponsiveContainer>
                    </div>
                    <div className="mt-6">
                      <h3 className="text-xs font-medium text-surface-400 mb-3">Average Price vs Market Price</h3>
                      <div className="h-48">
                        <ResponsiveContainer width="100%" height="100%">
                          <LineChart data={summary.chartData} margin={{ top: 5, right: 10, left: 0, bottom: 0 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                            <XAxis dataKey="date" tick={{ fill: '#64748b', fontSize: 10 }} tickFormatter={d => new Date(d).toLocaleDateString('en', { month: 'short', day: 'numeric' })} interval="preserveStartEnd" />
                            <YAxis tick={{ fill: '#64748b', fontSize: 10 }} />
                            <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', fontSize: '11px' }} formatter={(val: number, n: string) => [formatCurrency(val), n]} />
                            <Line type="monotone" dataKey="averagePrice" name="Avg Price" stroke="#f59e0b" strokeWidth={2} dot={false} />
                            <Line type="monotone" dataKey="marketPrice" name="Market Price" stroke="#06b6d4" strokeWidth={1.5} dot={false} strokeDasharray="4 2" />
                          </LineChart>
                        </ResponsiveContainer>
                      </div>
                    </div>
                  </>
                ) : (
                  <EmptyState title="No chart data" description="Execute DCA buys to see performance over time." />
                )}
              </div>
            ) : (
              <div className="overflow-x-auto">
                {summary.executions.length > 0 ? (
                  <table className="w-full text-xs">
                    <thead>
                      <tr className="border-b border-surface-700">
                        <th className="px-5 py-3 text-left font-medium text-surface-500 uppercase tracking-wider">Date</th>
                        <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Amount</th>
                        <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Price</th>
                        <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Quantity</th>
                        <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Type</th>
                      </tr>
                    </thead>
                    <tbody>
                      {summary.executions.map((exec, i) => (
                        <tr key={exec.id ?? i} className="border-b border-surface-800/50 hover:bg-surface-800/30 transition-colors">
                          <td className="px-5 py-3 font-mono">{exec.date}</td>
                          <td className="px-5 py-3 text-right font-mono">{formatCurrency(exec.amountPaid)}</td>
                          <td className="px-5 py-3 text-right font-mono">{formatCurrency(exec.priceAtExec)}</td>
                          <td className="px-5 py-3 text-right font-mono">{exec.quantity.toFixed(8)}</td>
                          <td className="px-5 py-3 text-right">
                            <span className={exec.simulated ? 'badge bg-surface-700 text-surface-400' : 'badge-green'}>
                              {exec.simulated ? 'Simulated' : 'Real'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <div className="p-8">
                    <EmptyState title="No executions" description="Log a manual execution or wait for simulation data." />
                  </div>
                )}
              </div>
            )}
          </div>
        </>
      ) : (
        <EmptyState title="No summary available" description="Summary data will appear after the plan has execution data." />
      )}

      {/* Add Execution Modal */}
      {showExec && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4" onClick={() => setShowExec(false)}>
          <div className="card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <h2 className="text-lg font-semibold mb-4">Log Manual Execution</h2>
            <div className="space-y-4">
              <div>
                <label className="label">Execution Date</label>
                <input type="date" className="input-field" value={execForm.executionDate} onChange={e => setExecForm(f => ({ ...f, executionDate: e.target.value }))} />
              </div>
              <div>
                <label className="label">Amount Paid ({plan?.baseCurrency})</label>
                <input type="number" step="0.01" placeholder={plan ? String(plan.amount) : '100'} className="input-field" value={execForm.amountPaid} onChange={e => setExecForm(f => ({ ...f, amountPaid: e.target.value }))} />
              </div>
              <div>
                <label className="label">Price at Execution</label>
                <input type="number" step="0.00000001" placeholder="97500" className="input-field" value={execForm.priceAtExec} onChange={e => setExecForm(f => ({ ...f, priceAtExec: e.target.value }))} />
              </div>
              {addExecMutation.error && <p className="text-xs text-accent-rose">{addExecMutation.error.message}</p>}
              <div className="flex gap-3">
                <button onClick={() => setShowExec(false)} className="btn-secondary flex-1">Cancel</button>
                <button onClick={handleAddExec} disabled={addExecMutation.isPending || !execForm.executionDate || !execForm.amountPaid || !execForm.priceAtExec} className="btn-primary flex-1">
                  {addExecMutation.isPending ? 'Saving...' : 'Save'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
