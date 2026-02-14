CREATE TABLE profissional (
                              id UUID PRIMARY KEY,
                              nome VARCHAR(255),
                              telefone VARCHAR(255),
                              usuario_id UUID,
                              CONSTRAINT fk_profissional_usuario
                                  FOREIGN KEY (usuario_id)
                                      REFERENCES usuario(id)
                                      ON DELETE CASCADE
);



CREATE TABLE veiculo (
                         id UUID PRIMARY KEY,
                         marca VARCHAR(255) NOT NULL,
                         ano INT,
                         id_profissional UUID NULL,
                         placa VARCHAR(10) not null,
                         CONSTRAINT fk_veiculo_profissional
                             FOREIGN KEY (id_profissional)
                                 REFERENCES profissional(id)
                                 ON DELETE SET NULL
);

CREATE TABLE empresa (
                         id UUID PRIMARY KEY,
                         id_usuario UUID not null,
                         nome VARCHAR(255) NOT NULL,
                         descricao TEXT,


                         CONSTRAINT fk_empresa_usuario
                             FOREIGN KEY (id_usuario)
                                 REFERENCES usuario(id)
                                 ON DELETE set null
);
CREATE TABLE viagem (
                        id UUID PRIMARY KEY,


                        id_veiculo UUID not null,
                        id_profissional UUID NOT NULL,
                        id_empresa UUID NOT NULL,

                        localizacao_frete VARCHAR(255) NOT NULL,

                        comissao DECIMAL(10,2) DEFAULT 0,
                        abastecimento DECIMAL(10,2) DEFAULT 0,
                        despesas DECIMAL(10,2) DEFAULT 0,

                        data_inicio TIMESTAMP,
                        data_fim TIMESTAMP,

                        status VARCHAR(50),

                        CONSTRAINT fk_viagem_profissional
                            FOREIGN KEY (id_profissional)
                                REFERENCES profissional(id)
                                ON DELETE set NULL

                                CONSTRAINT fk_viagem_empresa
                                FOREIGN KEY (id_empresa)
                                REFERENCES empresa(id)
                                ON DELETE set null


                                CONSTRAINT fk_viagem_veiculo
                                FOREIGN KEY (id_veiculo)
                                REFERENCES veiculo (id)
                                ON DELETE set null
);