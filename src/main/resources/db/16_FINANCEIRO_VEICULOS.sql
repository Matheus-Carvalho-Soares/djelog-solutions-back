-- Additive, rerunnable finance and fleet changes. Historical payment/comission values are untouched.
ALTER TABLE despesas
    ADD COLUMN IF NOT EXISTS data_despesa DATE,
    ADD COLUMN IF NOT EXISTS recebedor VARCHAR(150);

ALTER TABLE veiculo
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(20),
    ADD COLUMN IF NOT EXISTS modelo VARCHAR(80),
    ADD COLUMN IF NOT EXISTS id_profissional_proprietario UUID,
    ADD COLUMN IF NOT EXISTS id_caminhao UUID;

-- Existing fleet records keep their identity and are trucks by default.
UPDATE veiculo SET tipo = 'CAMINHAO' WHERE tipo IS NULL;
ALTER TABLE veiculo ALTER COLUMN tipo SET DEFAULT 'CAMINHAO';
ALTER TABLE veiculo ALTER COLUMN tipo SET NOT NULL;
ALTER TABLE veiculo ALTER COLUMN marca DROP NOT NULL;
ALTER TABLE veiculo ALTER COLUMN id_profissional DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_veiculo_proprietario') THEN
        ALTER TABLE veiculo ADD CONSTRAINT fk_veiculo_proprietario
            FOREIGN KEY (id_profissional_proprietario) REFERENCES profissional(id) ON DELETE SET NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_veiculo_caminhao') THEN
        ALTER TABLE veiculo ADD CONSTRAINT fk_veiculo_caminhao
            FOREIGN KEY (id_caminhao) REFERENCES veiculo(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_veiculo_caminhao
    ON veiculo(id_caminhao) WHERE id_caminhao IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_veiculo_proprietario
    ON veiculo(id_profissional_proprietario);
CREATE INDEX IF NOT EXISTS ix_veiculo_tipo
    ON veiculo(tipo);
CREATE INDEX IF NOT EXISTS ix_despesa_parcela_status_vencimento
    ON despesa_parcela(status_pagamento, data_vencimento);
