export interface User {
  id: number;
  email: string | null;
  displayName: string | null;
  planType: 'ANONYMOUS' | 'FREE' | 'PRO';
  hasAds: boolean;
  maxPortfolios: number;
  maxDcaPlans: number;
  maxHistoryDays: number;
}

export interface Asset {
  symbol: string;
  name: string;
  category: string;
  providerId: string;
}

export interface Portfolio {
  id: number;
  name: string;
  baseCurrency: string;
  positionCount: number;
  createdAt: string;
}

export interface Position {
  id: number;
  symbol: string;
  assetName: string;
  category: string;
  quantity: number;
  avgBuyPrice: number | null;
  currentPrice: number;
  currentValue: number;
  pnl: number | null;
  pnlPercent: number | null;
  allocationPercent: number;
  notes: string | null;
}

export interface AllocationItem {
  label: string;
  value: number | null;
  percent: number;
}

export interface RiskBreakdown {
  concentrationScore: number;
  categoryRiskScore: number;
  volatilityScore: number;
  drawdownScore: number;
}

export interface PortfolioAnalysis {
  totalValue: number;
  totalPnl: number;
  totalPnlPercent: number;
  riskScore: number;
  riskLevel: string;
  positions: Position[];
  allocationByAsset: AllocationItem[];
  allocationByCategory: AllocationItem[];
  riskBreakdown: RiskBreakdown;
  insights: string[];
}

export interface DcaPlan {
  id: number;
  symbol: string;
  assetName: string;
  amount: number;
  frequency: string;
  baseCurrency: string;
  startDate: string;
  endDate: string | null;
  active: boolean;
  executionCount: number;
  createdAt: string;
}

export interface DcaChartPoint {
  date: string;
  totalInvested: number;
  portfolioValue: number;
  averagePrice: number;
  marketPrice: number;
}

export interface DcaExecution {
  id: number;
  date: string;
  amountPaid: number;
  priceAtExec: number;
  quantity: number;
  simulated: boolean;
}

export interface DcaSummary {
  planId: number;
  symbol: string;
  totalInvested: number;
  totalQuantity: number;
  averagePrice: number;
  currentPrice: number;
  currentValue: number;
  pnl: number;
  pnlPercent: number;
  totalExecutions: number;
  chartData: DcaChartPoint[];
  executions: DcaExecution[];
}

export interface Alert {
  type: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'INFO';
  message: string;
  currentPrice: number;
  referencePrice: number;
  changePercent: number;
}
