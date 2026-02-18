ALTER TABLE viagem
    RENAME COLUMN localizacao_frete TO inicio_frete;

ALTER TABLE viagem
    ADD COLUMN IF NOT EXISTS fim_frete VARCHAR(255);

ALTER TABLE viagem
    DROP COLUMN abastecimento;

ALTER TABLE viagem
    DROP COLUMN despesas;
