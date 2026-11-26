-- =====================================================================
-- API Nutricional - schema completo (versão 1.1.0)
--
-- Use este arquivo para criar um banco NOVO, vazio.
-- Para atualizar um banco que já existe (ex.: o do Neon em produção),
-- NÃO rode este arquivo: rode os scripts de database/migracoes/ em ordem.
-- =====================================================================

-- Tabelas principais
CREATE TABLE categorias (
                            id_categoria SERIAL PRIMARY KEY,
                            nome_categoria VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE alimentos (
                           id_alimento SERIAL PRIMARY KEY,
                           nome_alimento VARCHAR,
                           calorias_por_100g DECIMAL,
                           id_categoria INTEGER,
                           CONSTRAINT fk_alimentos_categoria
                               FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

CREATE TABLE macro_nutrientes (
                                  id_macros SERIAL PRIMARY KEY,
                                  nome_macros VARCHAR
);

CREATE TABLE micro_nutrientes (
                                  id_micros SERIAL PRIMARY KEY,
                                  nome_micros VARCHAR,
                                  unidade VARCHAR(10)
);

-- Tabelas associativas (relações N:N)
CREATE TABLE alimento_macros (
                                 id_alimento INTEGER NOT NULL,
                                 id_macros INTEGER NOT NULL,
                                 gramas_por_100g DECIMAL,
                                 PRIMARY KEY (id_alimento, id_macros),
                                 FOREIGN KEY (id_alimento) REFERENCES alimentos(id_alimento),
                                 FOREIGN KEY (id_macros) REFERENCES macro_nutrientes(id_macros)
);

-- Atenção: apesar do nome histórico da coluna "microgramas_por_100g", o valor
-- guardado está na unidade indicada em micro_nutrientes.unidade
-- (mg para quase todos; mcg só para a Vitamina A).
CREATE TABLE alimento_micros (
                                 id_alimento INTEGER NOT NULL,
                                 id_micros INTEGER NOT NULL,
                                 microgramas_por_100g DECIMAL,
                                 PRIMARY KEY (id_alimento, id_micros),
                                 FOREIGN KEY (id_alimento) REFERENCES alimentos(id_alimento),
                                 FOREIGN KEY (id_micros) REFERENCES micro_nutrientes(id_micros)
);

-- Catálogo de nutrientes (necessário antes de rodar a aplicação,
-- já que o importador busca essas entradas pelo nome)
INSERT INTO macro_nutrientes (nome_macros) VALUES
                                               ('Proteína'), ('Lipídeos'), ('Carboidrato');

INSERT INTO micro_nutrientes (nome_micros, unidade) VALUES
                                                        ('Colesterol', 'mg'), ('Cálcio', 'mg'), ('Magnésio', 'mg'),
                                                        ('Manganês', 'mg'), ('Fósforo', 'mg'), ('Ferro', 'mg'),
                                                        ('Sódio', 'mg'), ('Potássio', 'mg'), ('Cobre', 'mg'),
                                                        ('Zinco', 'mg'), ('Vitamina A', 'mcg'), ('Tiamina', 'mg'),
                                                        ('Riboflavina', 'mg'), ('Piridoxina', 'mg'), ('Niacina', 'mg'),
                                                        ('Vitamina C', 'mg');

-- As categorias e os alimentos são gravados pelo importador da TACO
-- na primeira vez que a aplicação sobe com a tabela "alimentos" vazia.
