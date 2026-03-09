# DCA Guard 🛡️

**Smart DCA Planner + Portfolio Risk Analyzer for Cryptocurrencies**

Plan your dollar-cost averaging strategy, track performance, and analyze portfolio risk — all in one beautifully simple dashboard.

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────┐
│   React SPA     │────▶│  Spring Boot API  │────▶│ PostgreSQL │
│ Vite + Tailwind │     │  REST + Firebase  │     │            │
│ Recharts        │     │  JPA/Hibernate    │     │            │
└─────────────────┘     └──────────────────┘     └────────────┘
                              │
                              ▼
                        ┌────────────┐
                        │ CoinGecko  │
                        │  API (free)│
                        └────────────┘
```

## Quick Start

### With Docker Compose (recommended)

```bash
docker-compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

### Manual Setup

**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Features

### DCA Planner
- Create recurring investment plans (BTC, ETH, SOL...)
- Configure amount, frequency (daily/weekly/biweekly/monthly), currency (USD/BRL)
- Simulates DCA using historical prices from CoinGecko
- Log real executions manually
- Performance chart: total invested vs portfolio value
- Average price vs market price chart
- Dip Alert: price dropped X% in last 7 days
- Below Average Alert: current price below DCA average

### Portfolio Risk Analyzer
- Manual portfolio entry (asset + quantity + avg price)
- Real-time pricing from CoinGecko
- Risk Score 0-100 based on: concentration, category risk, 30-day volatility, 90-day max drawdown
- Allocation charts (pie by asset, bar by category)
- Educational insights (no financial advice)

### Monetization
- Google AdSense integration with placeholder fallback system
- Ads displayed on Dashboard, Portfolio List, and DCA Plans pages
- Sidebar ad placement in the main layout

### Internationalization (i18n)
- English and Portuguese (BR) support
- Auto-detects browser language
- Manual toggle in sidebar and mobile header
- Persisted language preference via localStorage

## Plan Limits

| Feature | Anonymous | Free |
|---------|-----------|------|
| Portfolios | 1 | 3 |
| DCA Plans | 2 | 10 |
| History | 7 days | 90 days |
| Ads | Yes | Yes |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Firebase token → upsert user |
| GET | `/api/auth/me` | Get current user |
| GET | `/api/market/assets?q=` | Search assets |
| GET | `/api/market/price?symbol=&currency=` | Current price |
| GET | `/api/market/history?symbol=&currency=&days=` | Price history |
| POST | `/api/portfolios` | Create portfolio |
| GET | `/api/portfolios` | List portfolios |
| GET | `/api/portfolios/{id}` | Get portfolio |
| POST | `/api/portfolios/{id}/positions` | Add position |
| DELETE | `/api/portfolios/{id}/positions/{posId}` | Remove position |
| GET | `/api/portfolios/{id}/analysis` | Full risk analysis |
| POST | `/api/dca/plans` | Create DCA plan |
| GET | `/api/dca/plans` | List plans |
| GET | `/api/dca/plans/{id}` | Get plan |
| POST | `/api/dca/plans/{id}/executions` | Log execution |
| GET | `/api/dca/plans/{id}/summary` | DCA summary + chart |
| GET | `/api/dca/plans/{id}/alerts` | Get alerts |

## Tech Stack

**Frontend:** React 18, TypeScript, Vite, TailwindCSS, React Router, React Query, Recharts, Lucide Icons

**Backend:** Java 21, Spring Boot 3, Spring Security, JPA/Hibernate, PostgreSQL

**Auth:** Firebase Authentication (Google + Anonymous)

**Market Data:** CoinGecko API (free tier)

**Ads:** Google AdSense with styled placeholder fallback

**i18n:** Custom context-based system (EN/PT-BR), no external libraries

**Infra:** Docker Compose (PostgreSQL + Backend + Frontend)

## Security

- Firebase JWT validation on all `/api/**` endpoints
- Data isolation by userId
- CORS configured for allowed origins
- Bean Validation on all request DTOs

## Deployment

Designed for Railway deployment (Hobby plan $5/month):
- PostgreSQL managed by Railway
- Backend and Frontend as separate services
- Firebase credentials via environment variables (base64 encoded)
- Custom domain with automatic SSL

## Disclaimer

DCA Guard is an educational and planning tool. All data, scores, and insights are for informational purposes only and do not constitute financial advice.