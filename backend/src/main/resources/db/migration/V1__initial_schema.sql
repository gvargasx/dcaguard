-- V1__initial_schema.sql

CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
                       firebase_uid    VARCHAR(128) NOT NULL UNIQUE,
                       email           VARCHAR(255),
                       display_name    VARCHAR(255),
                       plan_type       VARCHAR(20) NOT NULL DEFAULT 'ANONYMOUS'
                           CHECK (plan_type IN ('ANONYMOUS','FREE','PRO')),
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE assets (
                        id              BIGSERIAL PRIMARY KEY,
                        symbol          VARCHAR(20) NOT NULL UNIQUE,
                        name            VARCHAR(100) NOT NULL,
                        provider_id     VARCHAR(100) NOT NULL UNIQUE,
                        category        VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
                        image_url       VARCHAR(500),

    -- necessário pro Top 100 + “atualização”
                        market_cap_rank INT,
                        is_active       BOOLEAN NOT NULL DEFAULT TRUE,
                        last_synced_at  TIMESTAMPTZ,

                        created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE portfolios (
                            id              BIGSERIAL PRIMARY KEY,
                            user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            name            VARCHAR(100) NOT NULL,
                            base_currency   VARCHAR(5) NOT NULL DEFAULT 'USD'
                                CHECK (base_currency IN ('USD','BRL')),
                            created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                            updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_portfolios_user_id ON portfolios(user_id);

CREATE TABLE portfolio_positions (
                                     id              BIGSERIAL PRIMARY KEY,
                                     portfolio_id    BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
                                     asset_id        BIGINT NOT NULL REFERENCES assets(id),
                                     quantity        NUMERIC(24, 12) NOT NULL CHECK (quantity > 0),
                                     avg_buy_price   NUMERIC(24, 8) CHECK (avg_buy_price IS NULL OR avg_buy_price >= 0),
                                     notes           VARCHAR(500),
                                     created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     UNIQUE(portfolio_id, asset_id)
);

CREATE INDEX idx_positions_portfolio ON portfolio_positions(portfolio_id);

CREATE TABLE dca_plans (
                           id              BIGSERIAL PRIMARY KEY,
                           user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           asset_id        BIGINT NOT NULL REFERENCES assets(id),
                           amount          NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
                           frequency       VARCHAR(20) NOT NULL
                               CHECK (frequency IN ('WEEKLY','MONTHLY')),
                           base_currency   VARCHAR(5) NOT NULL DEFAULT 'USD'
                               CHECK (base_currency IN ('USD','BRL')),
                           start_date      DATE NOT NULL,
                           end_date        DATE,
                           active          BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_dca_plans_user_id ON dca_plans(user_id);
CREATE INDEX idx_dca_plans_user_active ON dca_plans(user_id, active);
CREATE INDEX idx_assets_active_rank ON assets(is_active, market_cap_rank);
CREATE INDEX idx_positions_asset_id ON portfolio_positions(asset_id);
CREATE INDEX idx_dca_plans_asset_id ON dca_plans(asset_id);

CREATE TABLE dca_executions (
                                id              BIGSERIAL PRIMARY KEY,
                                plan_id         BIGINT NOT NULL REFERENCES dca_plans(id) ON DELETE CASCADE,
                                execution_date  DATE NOT NULL,
                                amount_paid     NUMERIC(18, 2) NOT NULL CHECK (amount_paid > 0),
                                price_at_exec   NUMERIC(24, 8) NOT NULL CHECK (price_at_exec > 0),
                                quantity        NUMERIC(24, 12) NOT NULL CHECK (quantity > 0),
                                is_simulated    BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                                UNIQUE(plan_id, execution_date)
);

CREATE INDEX idx_dca_executions_plan ON dca_executions(plan_id);
CREATE INDEX idx_dca_exec_plan_date ON dca_executions(plan_id, execution_date DESC);

-- Seed mínimo (opcional). O Top 100 real vem do sync job.
INSERT INTO assets (symbol, name, provider_id, category, market_cap_rank, last_synced_at) VALUES
                                                                                              ('BTC', 'Bitcoin', 'bitcoin', 'LAYER1', 1, now()),
                                                                                              ('ETH', 'Ethereum', 'ethereum', 'LAYER1', 2, now()),
                                                                                              ('SOL', 'Solana', 'solana', 'LAYER1', 3, now())
    ON CONFLICT (provider_id) DO NOTHING;