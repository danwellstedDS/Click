CREATE TABLE access_scopes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type TEXT NOT NULL CHECK (type IN ('CHAIN','HOTEL','PORTFOLIO')),
    chain_id UUID NOT NULL REFERENCES chains(id) ON DELETE CASCADE,
    hotel_id UUID REFERENCES hotels(id) ON DELETE CASCADE,
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_access_scopes_chain_id_type ON access_scopes (chain_id, type);
-- Partial unique indexes (NULL-safe uniqueness per scope type)
CREATE UNIQUE INDEX uq_access_scope_chain
    ON access_scopes (chain_id) WHERE type = 'CHAIN';
CREATE UNIQUE INDEX uq_access_scope_hotel
    ON access_scopes (chain_id, hotel_id) WHERE type = 'HOTEL';
CREATE UNIQUE INDEX uq_access_scope_portfolio
    ON access_scopes (chain_id, portfolio_id) WHERE type = 'PORTFOLIO';
