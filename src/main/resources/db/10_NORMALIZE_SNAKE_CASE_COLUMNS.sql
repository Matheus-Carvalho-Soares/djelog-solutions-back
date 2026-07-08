DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'veiculo' AND column_name = 'qtdpeso'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'veiculo' AND column_name = 'qtd_peso'
    ) THEN
        ALTER TABLE veiculo RENAME COLUMN qtdpeso TO qtd_peso;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'veiculo' AND column_name = 'qtdpeso'
    ) THEN
        UPDATE veiculo SET qtd_peso = qtdpeso WHERE qtd_peso IS NULL;
        ALTER TABLE veiculo DROP COLUMN qtdpeso;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'nomecontato'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'nome_contato'
    ) THEN
        ALTER TABLE empresa RENAME COLUMN nomecontato TO nome_contato;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'nomecontato'
    ) THEN
        UPDATE empresa SET nome_contato = nomecontato WHERE nome_contato IS NULL;
        ALTER TABLE empresa DROP COLUMN nomecontato;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'telefonecontato'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'telefone_contato'
    ) THEN
        ALTER TABLE empresa RENAME COLUMN telefonecontato TO telefone_contato;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'telefonecontato'
    ) THEN
        UPDATE empresa SET telefone_contato = telefonecontato WHERE telefone_contato IS NULL;
        ALTER TABLE empresa DROP COLUMN telefonecontato;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'emailcontato'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'email_contato'
    ) THEN
        ALTER TABLE empresa RENAME COLUMN emailcontato TO email_contato;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'empresa' AND column_name = 'emailcontato'
    ) THEN
        UPDATE empresa SET email_contato = emailcontato WHERE email_contato IS NULL;
        ALTER TABLE empresa DROP COLUMN emailcontato;
    END IF;
END $$;
