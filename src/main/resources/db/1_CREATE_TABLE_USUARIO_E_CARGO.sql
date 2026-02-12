CREATE TABLE cargo (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       nome VARCHAR(255) NOT NULL,
                       observacao TEXT
);

CREATE TABLE usuario (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         nome VARCHAR(255) NOT NULL,
                         email VARCHAR(255) not null,
                         senha VARCHAR(255) not null,
                         id_cargo UUID,

                         CONSTRAINT fk_usuario_cargo
                             FOREIGN KEY (id_cargo)
                                 REFERENCES cargo(uuid)
                                 ON DELETE SET NULL
                                 ON UPDATE CASCADE
);