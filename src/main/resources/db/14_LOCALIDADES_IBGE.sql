ALTER TABLE viagem
    ADD COLUMN IF NOT EXISTS inicio_municipio_codigo BIGINT,
    ADD COLUMN IF NOT EXISTS inicio_uf_codigo BIGINT,
    ADD COLUMN IF NOT EXISTS parada_municipio_codigo BIGINT,
    ADD COLUMN IF NOT EXISTS parada_uf_codigo BIGINT,
    ADD COLUMN IF NOT EXISTS fim_municipio_codigo BIGINT,
    ADD COLUMN IF NOT EXISTS fim_uf_codigo BIGINT;
