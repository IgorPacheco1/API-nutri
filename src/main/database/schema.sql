-- Tabelas principais
CREATE TABLE alimentos (
                           id_alimento SERIAL PRIMARY KEY,
                           nome_alimento VARCHAR,
                           calorias_por_100g DECIMAL
);

CREATE TABLE macro_nutrientes (
                                  id_macros SERIAL PRIMARY KEY,
                                  nome_macros VARCHAR
);

CREATE TABLE micro_nutrientes (
                                  id_micros SERIAL PRIMARY KEY,
                                  nome_micros VARCHAR
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

INSERT INTO micro_nutrientes (nome_micros) VALUES
                                               ('Colesterol'), ('Cálcio'), ('Magnésio'), ('Manganês'), ('Fósforo'),
                                               ('Ferro'), ('Sódio'), ('Potássio'), ('Cobre'), ('Zinco'),
                                               ('Vitamina A'), ('Tiamina'), ('Riboflavina'), ('Piridoxina'),
                                               ('Niacina'), ('Vitamina C');