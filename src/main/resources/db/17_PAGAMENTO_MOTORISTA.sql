-- Driver commission control, legacy opening balances, and auditable payouts.
-- Existing trip commissions remain the expense source; payout rows only settle them.
CREATE TABLE IF NOT EXISTS controle_financeiro_motorista (
    id UUID PRIMARY KEY,
    profissional_id UUID NOT NULL UNIQUE REFERENCES profissional(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    data_inicio_controle DATE NOT NULL,
    saldo_inicial_pendente NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (saldo_inicial_pendente >= 0),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pagamento_motorista (
    id UUID PRIMARY KEY,
    profissional_id UUID NOT NULL REFERENCES profissional(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    data_pagamento DATE NOT NULL,
    valor NUMERIC(12, 2) NOT NULL CHECK (valor > 0),
    forma_pagamento VARCHAR(20) NOT NULL CHECK (forma_pagamento IN ('PIX', 'TRANSFERENCIA', 'DINHEIRO', 'OUTRO')),
    referencia VARCHAR(120),
    observacao VARCHAR(500),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pagamento_motorista_item (
    id UUID PRIMARY KEY,
    pagamento_id UUID NOT NULL REFERENCES pagamento_motorista(id) ON DELETE RESTRICT,
    viagem_id UUID REFERENCES viagem(id) ON DELETE RESTRICT,
    controle_id UUID REFERENCES controle_financeiro_motorista(id) ON DELETE RESTRICT,
    valor NUMERIC(12, 2) NOT NULL CHECK (valor > 0),
    CONSTRAINT ck_pagamento_motorista_item_origem CHECK (
        (viagem_id IS NOT NULL AND controle_id IS NULL) OR
        (viagem_id IS NULL AND controle_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_pagamento_motorista_item_viagem
    ON pagamento_motorista_item(pagamento_id, viagem_id) WHERE viagem_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_pagamento_motorista_item_controle
    ON pagamento_motorista_item(pagamento_id, controle_id) WHERE controle_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_pagamento_motorista_profissional_data
    ON pagamento_motorista(profissional_id, data_pagamento DESC);
CREATE INDEX IF NOT EXISTS ix_pagamento_motorista_item_viagem
    ON pagamento_motorista_item(viagem_id);
CREATE INDEX IF NOT EXISTS ix_pagamento_motorista_item_controle
    ON pagamento_motorista_item(controle_id);
