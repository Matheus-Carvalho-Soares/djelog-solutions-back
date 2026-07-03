CREATE TABLE usuario (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         nome VARCHAR(255) NOT NULL,
                         email VARCHAR(255) not null,
                         senha VARCHAR(255) not null
);
