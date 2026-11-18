-- PostgreSQL. Aplicar com backup antes da versão que usa DDL_AUTO=validate.
BEGIN;
ALTER TABLE anuncios ADD COLUMN IF NOT EXISTS codigo_peca varchar(40);
ALTER TABLE anuncios ADD COLUMN IF NOT EXISTS chave_publicacao varchar(64);
ALTER TABLE anuncios ADD COLUMN IF NOT EXISTS assinatura_publicacao varchar(64);
UPDATE anuncios SET codigo_peca = 'LEG-' || id::text WHERE codigo_peca IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_anuncios_loja_codigo ON anuncios(vendedor_id, codigo_peca);
CREATE UNIQUE INDEX IF NOT EXISTS uq_anuncios_loja_publicacao ON anuncios(vendedor_id, chave_publicacao);
COMMIT;
