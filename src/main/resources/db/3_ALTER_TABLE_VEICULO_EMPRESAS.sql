alter table veiculo
    add column if not exists
    nome varchar (100),
    add column if not exists
    qtdPeso DECIMAL (10,2);


alter table empresa
    add column if not exists
    nomeContato VARCHAR (100) not null,
    add column if not exists
    telefoneContato VARCHAR (100),
    add column if not exists
    emailContato VARCHAR (255);