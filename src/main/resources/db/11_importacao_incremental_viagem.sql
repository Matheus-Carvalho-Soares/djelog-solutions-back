ALTER TABLE viagem
    ADD COLUMN IF NOT EXISTS parada_intermediaria VARCHAR(120);

ALTER TABLE despesas
    ALTER COLUMN valor TYPE DECIMAL(10, 2)
    USING valor::DECIMAL(10, 2);

CREATE TABLE IF NOT EXISTS viagem_importacao (
    id UUID PRIMARY KEY,
    id_usuario UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    nome_arquivo VARCHAR(255) NOT NULL,
    hash_arquivo VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_viagem_importacao_usuario
    ON viagem_importacao(id_usuario, criado_em DESC);

CREATE TABLE IF NOT EXISTS viagem_importacao_linha (
    id UUID PRIMARY KEY,
    id_importacao UUID NOT NULL REFERENCES viagem_importacao(id) ON DELETE CASCADE,
    numero_linha INTEGER NOT NULL,
    data_viagem DATE,
    placa VARCHAR(20),
    empresa_original VARCHAR(255),
    inicio_frete VARCHAR(120),
    parada_intermediaria VARCHAR(120),
    fim_frete VARCHAR(120),
    valor_frete DECIMAL(10, 2),
    comissao DECIMAL(6, 2),
    abastecimento DECIMAL(10, 2),
    pedagio DECIMAL(10, 2),
    outras_despesas DECIMAL(10, 2),
    descricao_outras VARCHAR(500),
    avisos TEXT,
    situacao VARCHAR(30) NOT NULL,
    motivo TEXT,
    assinatura VARCHAR(64),
    selecionada BOOLEAN NOT NULL DEFAULT FALSE,
    id_veiculo UUID REFERENCES veiculo(id),
    id_profissional UUID REFERENCES profissional(id),
    id_empresa UUID REFERENCES empresa(id),
    status_viagem VARCHAR(50),
    id_viagem UUID REFERENCES viagem(id) ON DELETE SET NULL,
    CONSTRAINT uk_viagem_import_linha UNIQUE(id_importacao, numero_linha)
);

CREATE INDEX IF NOT EXISTS ix_viagem_import_linha_signature
    ON viagem_importacao_linha(assinatura);
CREATE INDEX IF NOT EXISTS ix_viagem_import_linha_trip
    ON viagem_importacao_linha(id_viagem);

ALTER TABLE viagem_importacao ENABLE ROW LEVEL SECURITY;
ALTER TABLE viagem_importacao_linha ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    REVOKE ALL ON TABLE viagem_importacao, viagem_importacao_linha FROM PUBLIC;
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL ON TABLE viagem_importacao, viagem_importacao_linha FROM anon;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL ON TABLE viagem_importacao, viagem_importacao_linha FROM authenticated;
    END IF;
END $$;
