CREATE TABLE cargo
(
    uuid       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome       VARCHAR(255) NOT NULL,
    observacao TEXT
);

CREATE TABLE usuario
(
    uuid     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome     VARCHAR(255) NOT NULL,
    id_cargo UUID,

    CONSTRAINT fk_usuario_cargo
        FOREIGN KEY (id_cargo)
            REFERENCES cargo (uuid)
            ON DELETE SET NULL
            ON UPDATE CASCADE
);
