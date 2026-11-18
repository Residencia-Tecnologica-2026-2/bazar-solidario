-- Revisar e aplicar em PostgreSQL após backup. Não contém credenciais.
CREATE TABLE IF NOT EXISTS arquivos_foto (
    nome varchar(50) PRIMARY KEY,
    tipo varchar(30) NOT NULL,
    conteudo bytea NOT NULL
);
CREATE TABLE IF NOT EXISTS mercado_livre_contas (
    usuario_id bigint PRIMARY KEY,
    vendedor_externo bigint NOT NULL UNIQUE,
    apelido varchar(200) NOT NULL,
    access_token_cifrado varchar(4096) NOT NULL,
    refresh_token_cifrado varchar(4096) NOT NULL,
    expira_em timestamp(6) with time zone NOT NULL
);
CREATE TABLE IF NOT EXISTS mercado_livre_autorizacoes (
    hash varchar(64) PRIMARY KEY,
    usuario_id bigint NOT NULL,
    verificador_cifrado varchar(512) NOT NULL,
    expira_em timestamp(6) with time zone NOT NULL
);
CREATE TABLE IF NOT EXISTS mercado_livre_vinculos (
    anuncio_id bigint PRIMARY KEY,
    usuario_id bigint NOT NULL,
    item_id varchar(30) NOT NULL UNIQUE,
    status_externo varchar(30) NOT NULL,
    estoque_externo integer NOT NULL,
    consultado_em timestamp(6) with time zone NOT NULL
);
CREATE TABLE IF NOT EXISTS mercado_livre_publicacoes (
    anuncio_id bigint PRIMARY KEY,
    usuario_id bigint NOT NULL,
    estado varchar(30) NOT NULL,
    item_id varchar(30),
    aviso varchar(250) NOT NULL,
    atualizado_em timestamp(6) with time zone NOT NULL
);
