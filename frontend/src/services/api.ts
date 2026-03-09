import type { Asset, Portfolio, PortfolioAnalysis, Position, DcaPlan, DcaSummary, Alert, User } from '../types';

const BASE_URL = import.meta.env.VITE_API_URL || '';

function getHeaders(): HeadersInit {
  const headers: HeadersInit = { 'Content-Type': 'application/json' };
  const token = localStorage.getItem('firebase_token');
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers: { ...getHeaders(), ...options?.headers },
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ detail: 'Request failed' }));
    throw new Error(error.detail || `HTTP ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

// Auth
export const authApi = {
  login: () => request<User>('/api/auth/login', { method: 'POST' }),
  me: () => request<User>('/api/auth/me'),
};

// Market
export const marketApi = {
  searchAssets: (q = '') => request<Asset[]>(`/api/market/assets?q=${encodeURIComponent(q)}`),
  getPrice: (symbol: string, currency = 'USD') =>
    request<{ symbol: string; currency: string; price: number }>(`/api/market/price?symbol=${symbol}&currency=${currency}`),
  getHistory: (symbol: string, currency = 'USD', days = 90) =>
    request<{ date: string; price: number }[]>(`/api/market/history?symbol=${symbol}&currency=${currency}&days=${days}`),
};

// Portfolios
export const portfolioApi = {
  list: () => request<Portfolio[]>('/api/portfolios'),
  get: (id: number) => request<Portfolio>(`/api/portfolios/${id}`),
  create: (data: { name: string; baseCurrency: string }) =>
    request<Portfolio>('/api/portfolios', { method: 'POST', body: JSON.stringify(data) }),
  addPosition: (portfolioId: number, data: { symbol: string; quantity: number; avgBuyPrice?: number; notes?: string }) =>
    request<Position>(`/api/portfolios/${portfolioId}/positions`, { method: 'POST', body: JSON.stringify(data) }),
  deletePosition: (portfolioId: number, positionId: number) =>
    request<void>(`/api/portfolios/${portfolioId}/positions/${positionId}`, { method: 'DELETE' }),
  analyze: (portfolioId: number) =>
    request<PortfolioAnalysis>(`/api/portfolios/${portfolioId}/analysis`),
};

// DCA Plans
export const dcaApi = {
  list: () => request<DcaPlan[]>('/api/dca/plans'),
  get: (id: number) => request<DcaPlan>(`/api/dca/plans/${id}`),
  create: (data: { symbol: string; amount: number; frequency: string; baseCurrency: string; startDate: string; endDate?: string }) =>
    request<DcaPlan>('/api/dca/plans', { method: 'POST', body: JSON.stringify(data) }),
  addExecution: (planId: number, data: { executionDate: string; amountPaid: number; priceAtExec: number }) =>
    request<any>(`/api/dca/plans/${planId}/executions`, { method: 'POST', body: JSON.stringify(data) }),
  getSummary: (planId: number) => request<DcaSummary>(`/api/dca/plans/${planId}/summary`),
  getAlerts: (planId: number) => request<Alert[]>(`/api/dca/plans/${planId}/alerts`),
};
