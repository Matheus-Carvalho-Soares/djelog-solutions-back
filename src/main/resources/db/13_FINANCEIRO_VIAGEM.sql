-- Commission override and editable advance split. Existing commission percentages are preserved.
ALTER TABLE viagem
    ADD COLUMN IF NOT EXISTS valor_comissao_manual NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS percentual_adiantamento NUMERIC(5, 2);

UPDATE viagem
SET percentual_adiantamento = 70.00
WHERE percentual_adiantamento IS NULL;

ALTER TABLE viagem
    ALTER COLUMN percentual_adiantamento SET DEFAULT 70.00,
    ALTER COLUMN percentual_adiantamento SET NOT NULL;

-- Expenses may belong to a trip, to a truck, or only to the user.
ALTER TABLE despesas ALTER COLUMN id_viagem DROP NOT NULL;
ALTER TABLE despesas ALTER COLUMN valor TYPE NUMERIC(12, 2) USING COALESCE(valor, 0)::NUMERIC(12, 2);
ALTER TABLE despesas ADD COLUMN IF NOT EXISTS usuario_id UUID;
ALTER TABLE despesas ADD COLUMN IF NOT EXISTS id_veiculo UUID;
ALTER TABLE despesas ADD COLUMN IF NOT EXISTS categoria VARCHAR(40);

UPDATE despesas d
SET usuario_id = p.usuario_id,
    id_veiculo = v.id_veiculo
FROM viagem v
JOIN profissional p ON p.id = v.id_profissional
WHERE d.id_viagem = v.id
  AND (d.usuario_id IS NULL OR d.id_veiculo IS NULL);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_despesas_usuario') THEN
        ALTER TABLE despesas ADD CONSTRAINT fk_despesas_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_despesas_veiculo') THEN
        ALTER TABLE despesas ADD CONSTRAINT fk_despesas_veiculo FOREIGN KEY (id_veiculo) REFERENCES veiculo(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_despesas_usuario ON despesas(usuario_id);
CREATE INDEX IF NOT EXISTS ix_despesas_veiculo ON despesas(id_veiculo);
CREATE INDEX IF NOT EXISTS ix_despesas_viagem ON despesas(id_viagem);

CREATE TABLE IF NOT EXISTS despesa_parcela (
    id UUID PRIMARY KEY,
    despesa_id UUID NOT NULL REFERENCES despesas(id) ON DELETE CASCADE,
    valor NUMERIC(12, 2) NOT NULL,
    data_vencimento DATE,
    status_pagamento VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_pagamento DATE,
    numero_parcela INTEGER NOT NULL DEFAULT 1
);

-- Keep historical expense totals and mark their payment state as unknown.
INSERT INTO despesa_parcela (id, despesa_id, valor, data_vencimento, status_pagamento, numero_parcela)
SELECT gen_random_uuid(), d.id, COALESCE(d.valor, 0), v.data_inicio::DATE, 'NAO_INFORMADO', 1
FROM despesas d
LEFT JOIN viagem v ON v.id = d.id_viagem
WHERE NOT EXISTS (SELECT 1 FROM despesa_parcela p WHERE p.despesa_id = d.id);

CREATE INDEX IF NOT EXISTS ix_despesa_parcela_vencimento ON despesa_parcela(data_vencimento);
CREATE INDEX IF NOT EXISTS ix_despesa_parcela_status ON despesa_parcela(status_pagamento);

CREATE TABLE IF NOT EXISTS recebimento_frete (
    id UUID PRIMARY KEY,
    viagem_id UUID NOT NULL REFERENCES viagem(id) ON DELETE CASCADE,
    valor NUMERIC(12, 2) NOT NULL CHECK (valor > 0),
    data_recebimento DATE NOT NULL,
    observacao VARCHAR(500),
    inicial BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE recebimento_frete
    ADD COLUMN IF NOT EXISTS inicial BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS ix_recebimento_frete_viagem ON recebimento_frete(viagem_id);
CREATE INDEX IF NOT EXISTS ix_recebimento_frete_data ON recebimento_frete(data_recebimento);
CREATE UNIQUE INDEX IF NOT EXISTS ux_recebimento_frete_inicial ON recebimento_frete(viagem_id) WHERE inicial = TRUE;
