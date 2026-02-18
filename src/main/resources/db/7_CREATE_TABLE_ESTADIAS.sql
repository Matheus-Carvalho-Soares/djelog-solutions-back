
create table estadia
(
    id        UUID primary key,
    id_viagem UUID not null,
    descricao TEXT,
    valor     INT,
    constraint fk_despesas_viagem
        foreign KEY (id_viagem)
            references viagem (id)
            on delete set null
);

alter table viagem_comissionada add column descricao TEXT;