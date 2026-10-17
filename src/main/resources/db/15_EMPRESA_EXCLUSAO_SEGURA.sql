-- Deleting a company removes only its optional links; trips and import rows stay intact.
ALTER TABLE viagem ALTER COLUMN id_empresa DROP NOT NULL;
ALTER TABLE viagem_importacao_linha ALTER COLUMN id_empresa DROP NOT NULL;

DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        WHERE c.conrelid = 'viagem'::regclass
          AND c.confrelid = 'empresa'::regclass
          AND c.contype = 'f'
          AND pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY (id_empresa)%'
    LOOP
        EXECUTE format('ALTER TABLE viagem DROP CONSTRAINT %I', constraint_name);
    END LOOP;

    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        WHERE c.conrelid = 'viagem_importacao_linha'::regclass
          AND c.confrelid = 'empresa'::regclass
          AND c.contype = 'f'
          AND pg_get_constraintdef(c.oid) LIKE 'FOREIGN KEY (id_empresa)%'
    LOOP
        EXECUTE format('ALTER TABLE viagem_importacao_linha DROP CONSTRAINT %I', constraint_name);
    END LOOP;

    ALTER TABLE viagem
        ADD CONSTRAINT fk_viagem_empresa
        FOREIGN KEY (id_empresa) REFERENCES empresa(id) ON DELETE SET NULL;
    ALTER TABLE viagem_importacao_linha
        ADD CONSTRAINT fk_viagem_importacao_linha_empresa
        FOREIGN KEY (id_empresa) REFERENCES empresa(id) ON DELETE SET NULL;
END $$;
