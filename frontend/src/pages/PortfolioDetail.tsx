import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Plus, Trash2, Info, AlertTriangle } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts';
import { portfolioApi, marketApi } from '../services/api';
import { formatCurrency, formatPercent, getRiskColor, getRiskBgColor, getPnlColor } from '../utils/format';
import { LoadingSpinner, EmptyState, ErrorMessage } from '../components/common/States';
import AdBanner from '../components/ads/AdBanner';
import type { Asset } from '../types';

const PIE_COLORS = ['#22c55e', '#06b6d4', '#8b5cf6', '#f59e0b', '#f43f5e', '#ec4899'];

export default function PortfolioDetail() {
  const { id } = useParams();
  const portfolioId = Number(id);
  const queryClient = useQueryClient();
  const [showAddPosition, setShowAddPosition] = useState(false);
  const [posForm, setPosForm] = useState({ symbol: '', quantity: '', avgBuyPrice: '' });
  const [assetSearch, setAssetSearch] = useState('');

  const { data: portfolio, isLoading: loadingPortfolio } = useQuery({
    queryKey: ['portfolio', portfolioId],
    queryFn: () => portfolioApi.get(portfolioId),
  });

  const { data: analysis, isLoading: loadingAnalysis, error, refetch } = useQuery({
    queryKey: ['portfolioAnalysis', portfolioId],
    queryFn: () => portfolioApi.analyze(portfolioId),
  });

  const { data: assets } = useQuery({
    queryKey: ['assets', assetSearch],
    queryFn: () => marketApi.searchAssets(assetSearch),
    enabled: showAddPosition,
  });

  const addPositionMutation = useMutation({
    mutationFn: (data: { symbol: string; quantity: number; avgBuyPrice?: number }) =>
      portfolioApi.addPosition(portfolioId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolioAnalysis', portfolioId] });
      queryClient.invalidateQueries({ queryKey: ['portfolio', portfolioId] });
      setShowAddPosition(false);
      setPosForm({ symbol: '', quantity: '', avgBuyPrice: '' });
    },
  });

  const deletePositionMutation = useMutation({
    mutationFn: (positionId: number) => portfolioApi.deletePosition(portfolioId, positionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolioAnalysis', portfolioId] });
      queryClient.invalidateQueries({ queryKey: ['portfolio', portfolioId] });
    },
  });

  const handleAddPosition = () => {
    if (!posForm.symbol || !posForm.quantity) return;
    addPositionMutation.mutate({
      symbol: posForm.symbol,
      quantity: parseFloat(posForm.quantity),
      avgBuyPrice: posForm.avgBuyPrice ? parseFloat(posForm.avgBuyPrice) : undefined,
    });
  };

  if (loadingPortfolio || loadingAnalysis) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error.message} onRetry={() => refetch()} />;
  if (!analysis) return <ErrorMessage message="Analysis not available" />;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Link to="/app/portfolio" className="btn-ghost p-2"><ArrowLeft className="w-4 h-4" /></Link>
        <div className="flex-1">
          <h1 className="text-xl font-display font-bold">{portfolio?.name || 'Portfolio'}</h1>
          <p className="text-xs text-surface-500">{analysis.positions.length} positions · {portfolio?.baseCurrency}</p>
        </div>
        <button onClick={() => setShowAddPosition(true)} className="btn-primary text-xs">
          <Plus className="w-4 h-4" /> Add Position
        </button>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card p-4">
          <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Total Value</p>
          <p className="text-lg font-display font-bold">{formatCurrency(analysis.totalValue)}</p>
        </div>
        <div className="card p-4">
          <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Total PnL</p>
          <p className={`text-lg font-display font-bold ${getPnlColor(analysis.totalPnl)}`}>
            {formatCurrency(analysis.totalPnl)}
          </p>
          <p className={`text-xs font-mono ${getPnlColor(analysis.totalPnlPercent)}`}>
            {formatPercent(analysis.totalPnlPercent)}
          </p>
        </div>
        <div className="card p-4">
          <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Risk Score</p>
          <p className={`text-lg font-display font-bold ${getRiskColor(analysis.riskScore)}`}>
            {analysis.riskScore}/100
          </p>
          <p className={`text-xs ${getRiskColor(analysis.riskScore)}`}>{analysis.riskLevel}</p>
        </div>
        <div className="card p-4">
          <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-1">Positions</p>
          <p className="text-lg font-display font-bold">{analysis.positions.length}</p>
        </div>
      </div>

      <AdBanner placement="inline" />

      {/* Risk Score Breakdown */}
      {analysis.riskBreakdown && (
        <div className="card p-5">
          <h2 className="section-title flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-accent-amber" /> Risk Analysis
          </h2>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            {[
              { label: 'Concentration', score: analysis.riskBreakdown.concentrationScore },
              { label: 'Category Risk', score: analysis.riskBreakdown.categoryRiskScore },
              { label: 'Volatility 30d', score: analysis.riskBreakdown.volatilityScore },
              { label: 'Drawdown 90d', score: analysis.riskBreakdown.drawdownScore },
            ].map(item => (
              <div key={item.label} className="text-center">
                <p className="text-[10px] uppercase tracking-wider text-surface-500 mb-2">{item.label}</p>
                <div className="relative w-full h-2 bg-surface-700 rounded-full overflow-hidden mb-1">
                  <div
                    className={`absolute left-0 top-0 h-full rounded-full transition-all duration-700 ${getRiskBgColor(item.score)}`}
                    style={{ width: `${item.score}%` }}
                  />
                </div>
                <p className={`text-sm font-mono font-bold ${getRiskColor(item.score)}`}>{item.score}</p>
              </div>
            ))}
          </div>
          {analysis.insights && analysis.insights.length > 0 && (
            <div className="space-y-2">
              {analysis.insights.map((insight, i) => (
                <div key={i} className="flex items-start gap-2 p-3 rounded-lg bg-surface-800/50">
                  <Info className="w-4 h-4 text-accent-cyan mt-0.5 flex-shrink-0" />
                  <p className="text-xs text-surface-300 leading-relaxed">{insight}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Charts */}
      {analysis.positions.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="card p-5">
            <h2 className="section-title">Allocation by Asset</h2>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={analysis.allocationByAsset} dataKey="percent" nameKey="label" cx="50%" cy="50%" outerRadius={80} innerRadius={50} strokeWidth={2} stroke="#0f172a">
                    {analysis.allocationByAsset.map((_, i) => (
                      <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', fontSize: '12px' }} formatter={(val: number) => `${val.toFixed(1)}%`} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="flex flex-wrap gap-3 mt-2 justify-center">
              {analysis.allocationByAsset.map((item, i) => (
                <div key={item.label} className="flex items-center gap-1.5 text-xs">
                  <div className="w-2.5 h-2.5 rounded-sm" style={{ backgroundColor: PIE_COLORS[i % PIE_COLORS.length] }} />
                  <span className="text-surface-300">{item.label}</span>
                  <span className="font-mono text-surface-500">{item.percent.toFixed(1)}%</span>
                </div>
              ))}
            </div>
          </div>

          <div className="card p-5">
            <h2 className="section-title">Allocation by Category</h2>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={analysis.allocationByCategory} layout="vertical">
                  <XAxis type="number" domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 11 }} tickFormatter={v => `${v}%`} />
                  <YAxis type="category" dataKey="label" tick={{ fill: '#94a3b8', fontSize: 11 }} width={80} />
                  <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', fontSize: '12px' }} formatter={(val: number) => `${val.toFixed(1)}%`} />
                  <Bar dataKey="percent" fill="#22c55e" radius={[0, 6, 6, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}

      {/* Positions Table */}
      {analysis.positions.length > 0 ? (
        <div className="card overflow-hidden">
          <div className="p-5 pb-3"><h2 className="section-title mb-0">Positions</h2></div>
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-surface-700">
                  <th className="px-5 py-3 text-left font-medium text-surface-500 uppercase tracking-wider">Asset</th>
                  <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Quantity</th>
                  <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Avg Price</th>
                  <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Current</th>
                  <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">Value</th>
                  <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider">PnL</th>
                  <th className="px-5 py-3 text-right font-medium text-surface-500 uppercase tracking-wider"></th>
                </tr>
              </thead>
              <tbody>
                {analysis.positions.map(pos => (
                  <tr key={pos.id} className="border-b border-surface-800/50 hover:bg-surface-800/30 transition-colors">
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-surface-700 flex items-center justify-center text-[10px] font-bold font-mono">{pos.symbol}</div>
                        <div>
                          <p className="font-medium">{pos.assetName}</p>
                          <p className="text-surface-500">{pos.category}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-3 text-right font-mono">{pos.quantity}</td>
                    <td className="px-5 py-3 text-right font-mono text-surface-400">{pos.avgBuyPrice ? formatCurrency(pos.avgBuyPrice) : '—'}</td>
                    <td className="px-5 py-3 text-right font-mono">{formatCurrency(pos.currentPrice)}</td>
                    <td className="px-5 py-3 text-right font-mono font-medium">{formatCurrency(pos.currentValue)}</td>
                    <td className={`px-5 py-3 text-right font-mono ${getPnlColor(pos.pnl)}`}>
                      {pos.pnl !== null ? `${formatCurrency(pos.pnl)} (${formatPercent(pos.pnlPercent!)})` : '—'}
                    </td>
                    <td className="px-5 py-3 text-right">
                      <button
                        onClick={() => deletePositionMutation.mutate(pos.id)}
                        className="p-1.5 rounded-lg hover:bg-surface-700 text-surface-500 hover:text-accent-rose transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <EmptyState
          title="No positions"
          description="Add your first crypto position to see allocation and risk analysis."
          action={<button onClick={() => setShowAddPosition(true)} className="btn-primary text-xs"><Plus className="w-3.5 h-3.5" /> Add Position</button>}
        />
      )}

      {/* Add Position Modal */}
      {showAddPosition && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4" onClick={() => setShowAddPosition(false)}>
          <div className="card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <h2 className="text-lg font-semibold mb-4">Add Position</h2>
            <div className="space-y-4">
              <div>
                <label className="label">Asset</label>
                <select
                  className="input-field"
                  value={posForm.symbol}
                  onChange={e => setPosForm(f => ({ ...f, symbol: e.target.value }))}
                >
                  <option value="">Select an asset...</option>
                  {assets?.map(a => (
                    <option key={a.symbol} value={a.symbol}>{a.symbol} — {a.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Quantity</label>
                <input type="number" step="any" placeholder="0.5" className="input-field" value={posForm.quantity} onChange={e => setPosForm(f => ({ ...f, quantity: e.target.value }))} />
              </div>
              <div>
                <label className="label">Average Buy Price (optional)</label>
                <input type="number" step="any" placeholder="65000" className="input-field" value={posForm.avgBuyPrice} onChange={e => setPosForm(f => ({ ...f, avgBuyPrice: e.target.value }))} />
              </div>
              {addPositionMutation.error && <p className="text-xs text-accent-rose">{addPositionMutation.error.message}</p>}
              <div className="flex gap-3">
                <button onClick={() => setShowAddPosition(false)} className="btn-secondary flex-1">Cancel</button>
                <button onClick={handleAddPosition} disabled={addPositionMutation.isPending || !posForm.symbol || !posForm.quantity} className="btn-primary flex-1">
                  {addPositionMutation.isPending ? 'Adding...' : 'Add'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
