
create table despesas
(
    id        UUID primary key,
    id_viagem UUID not null,
    nome      VARCHAR(100),
    descricao TEXT,
    valor     INT,
    constraint fk_despesas_viagem
        foreign KEY (id_viagem)
            references viagem (id)
            on delete set null
);


create table viagem_comissionada
(
    id        UUID primary key,
    inicio_frete      VARCHAR(100),
    fim_frete VARCHAR(100),
    valor INT,
    comissao INT
);