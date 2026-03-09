import { clsx, type ClassValue } from 'clsx';

export function cn(...inputs: ClassValue[]) {
  return clsx(inputs);
}

export function formatCurrency(value: number, currency = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
}

export function formatNumber(value: number, decimals = 2): string {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
}

export function formatPercent(value: number): string {
  const sign = value >= 0 ? '+' : '';
  return `${sign}${value.toFixed(2)}%`;
}

export function formatCrypto(value: number): string {
  if (value >= 1) return formatNumber(value, 4);
  if (value >= 0.001) return formatNumber(value, 6);
  return formatNumber(value, 8);
}

export function getRiskColor(score: number): string {
  if (score <= 20) return 'text-brand-400';
  if (score <= 40) return 'text-brand-300';
  if (score <= 60) return 'text-accent-amber';
  if (score <= 80) return 'text-orange-400';
  return 'text-accent-rose';
}

export function getRiskBgColor(score: number): string {
  if (score <= 20) return 'bg-brand-500';
  if (score <= 40) return 'bg-brand-400';
  if (score <= 60) return 'bg-accent-amber';
  if (score <= 80) return 'bg-orange-400';
  return 'bg-accent-rose';
}

export function getPnlColor(value: number | null): string {
  if (value === null) return 'text-surface-400';
  return value >= 0 ? 'text-brand-400' : 'text-accent-rose';
}

export function getSeverityColor(severity: string): string {
  switch (severity) {
    case 'HIGH': return 'text-accent-rose';
    case 'MEDIUM': return 'text-accent-amber';
    case 'LOW': return 'text-brand-400';
    default: return 'text-accent-cyan';
  }
}
