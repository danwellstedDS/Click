CREATE TABLE portfolios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chain_id UUID NOT NULL REFERENCES chains(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    owner_organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (chain_id, name)
);
CREATE INDEX idx_portfolios_chain_id ON portfolios (chain_id);
CREATE INDEX idx_portfolios_owner_organization_id ON portfolios (owner_organization_id);

CREATE TABLE portfolio_hotels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    hotel_id UUID NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    UNIQUE (portfolio_id, hotel_id)
);
CREATE INDEX idx_portfolio_hotels_hotel_id ON portfolio_hotels (hotel_id);
