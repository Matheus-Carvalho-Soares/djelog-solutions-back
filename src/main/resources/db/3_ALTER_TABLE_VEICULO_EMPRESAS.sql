alter table veiculo
    add column if not exists
    nome varchar (100),
    add column if not exists
    qtd_peso DECIMAL (10,2);


alter table empresa
    add column if not exists
    nome_contato VARCHAR (100) not null,
    add column if not exists
    telefone_contato VARCHAR (100),
    add column if not exists
    email_contato VARCHAR (255);
