ALTER TABLE viagem_comissionada
    ADD COLUMN IF NOT EXISTS id_usuario UUID;

ALTER TABLE viagem_comissionada
    ADD CONSTRAINT fk_viagem_comissionada_usuario
    FOREIGN KEY (id_usuario)
    REFERENCES usuario (id);

