-- =====================================================================
-- Migração para a versão 1.1.0 da API Nutricional
--
-- Para quem: bancos criados com o schema.sql da versão 1.0.0
--            (ex.: o banco do Neon que já está em produção).
-- O que faz:
--   1. cria a tabela "categorias";
--   2. adiciona alimentos.id_categoria (chave estrangeira para categorias);
--   3. adiciona micro_nutrientes.unidade e preenche a unidade de cada
--      micronutriente (mg, ou mcg para a Vitamina A).
--
-- Pode ser rodado mais de uma vez sem erro (é idempotente).
-- Rode ANTES de publicar a versão 1.1.0 no Render: a versão nova usa
-- ddl-auto: validate e não sobe se essas colunas não existirem.
-- A versão 1.0.0 continua funcionando depois deste script, porque ele só
-- acrescenta tabelas e colunas.
--
-- As categorias dos alimentos já cadastrados não são preenchidas aqui:
-- na primeira subida da versão 1.1.0, o importador lê o CSV da TACO e
-- completa as categorias que estiverem vazias (casando pelo nome).
-- =====================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS categorias (
    id_categoria SERIAL PRIMARY KEY,
    nome_categoria VARCHAR(100) NOT NULL UNIQUE
);

ALTER TABLE alimentos
    ADD COLUMN IF NOT EXISTS id_categoria INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_alimentos_categoria'
    ) THEN
        ALTER TABLE alimentos
            ADD CONSTRAINT fk_alimentos_categoria
            FOREIGN KEY (id_categoria) REFERENCES categorias (id_categoria);
    END IF;
END $$;

ALTER TABLE micro_nutrientes
    ADD COLUMN IF NOT EXISTS unidade VARCHAR(10);

-- Na TACO, todos os micronutrientes importados estão em mg, menos a
-- Vitamina A (RAE), que está em mcg.
UPDATE micro_nutrientes
SET unidade = CASE WHEN nome_micros = 'Vitamina A' THEN 'mcg' ELSE 'mg' END
WHERE unidade IS NULL;

COMMIT;
