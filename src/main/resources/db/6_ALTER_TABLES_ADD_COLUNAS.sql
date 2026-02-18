-- Adiciona coluna valor_frete na tabela viagem (usada na entidade Viagem.valorFrete)
ALTER TABLE viagem
    ADD COLUMN IF NOT EXISTS valor_frete DECIMAL(10,2);

ALTER TABLE viagem
    ADD COLUMN IF NOT EXISTS qtdCarga DECIMAL(10,2);


-- Adiciona coluna status na tabela veiculo (usada na entidade Veiculo.status)
ALTER TABLE veiculo
    ADD COLUMN IF NOT EXISTS status BOOLEAN;

-- Adiciona coluna status na tabela profissional (usada na entidade Profissional.status)
ALTER TABLE profissional
    ADD COLUMN IF NOT EXISTS status BOOLEAN;
