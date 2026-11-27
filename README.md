# API Nutricional

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-336791?logo=postgresql&logoColor=white)
![Versão](https://img.shields.io/badge/vers%C3%A3o-1.1.0-blue)

API REST em Spring Boot que fornece dados nutricionais (calorias, macro e
micronutrientes) de alimentos com base na **Tabela TACO (UNICAMP)** e calcula
os totais nutricionais proporcionais a uma quantidade informada.

Desenvolvida como projeto de portfólio e como fonte de dados nutricionais do
**PratoFácil**, aplicativo de acompanhamento nutricional do Trabalho
Interdisciplinar de ADS da PUC Minas (Betim).

**API em produção:** [api-nutri-uu2q.onrender.com](https://api-nutri-uu2q.onrender.com) ·
**Documentação (Swagger):** [/swagger-ui/index.html](https://api-nutri-uu2q.onrender.com/swagger-ui/index.html)

---

## Sumário

- [Sobre](#sobre)
- [Funcionalidades](#funcionalidades)
- [Rotas](#rotas)
- [Campos e unidades](#campos-e-unidades)
- [Erros](#erros)
- [Carga do catálogo no PratoFácil (MySQL)](#carga-do-catálogo-no-pratofácil-mysql)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Como rodar localmente](#como-rodar-localmente)
- [Testes](#testes)
- [Atualizando o banco e a publicação no Render](#atualizando-o-banco-e-a-publicação-no-render)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Fonte dos dados](#fonte-dos-dados)
- [Histórico de versões](#histórico-de-versões)
- [Autor](#autor)

## Sobre

A API Nutricional resolve um problema comum de apps de nutrição: dado um
conjunto de alimentos e suas porções (em gramas), calcular o total de
calorias, macronutrientes e micronutrientes — sem que a aplicação cliente
precise montar essa base de dados por conta própria.

Ela também exporta o catálogo completo da TACO (597 alimentos, 15 categorias)
em formatos prontos para carga em outro banco, que é a forma recomendada de uso
pelo PratoFácil: o app consulta o próprio MySQL, fica rápido e funciona offline,
sem depender desta API a cada busca.

## Funcionalidades

- Busca de alimentos por nome que ignora acentos, maiúsculas e pontuação
  (`feijao` encontra `Feijão, carioca, cozido`), com limite de resultados
- Cada alimento vem com categoria e valores por 100 g de kcal, carboidrato,
  proteína, gordura e sódio — o cliente calcula qualquer porção sem nova chamada
- Detalhe de um alimento com todos os micronutrientes e a unidade de cada um
- Exportação do catálogo em JSON, CSV e script MySQL
- Cálculo de calorias, macros e micros de uma refeição
- Validação da entrada com erro 400 legível
- CORS liberado só para as origens configuradas

## Rotas

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/alimentos?nome=&limite=` | Busca por nome. `nome`: 1 a 100 caracteres. `limite`: 1 a 50 (padrão 20) |
| `GET` | `/alimentos/{id}` | Detalhe de um alimento, com micronutrientes |
| `GET` | `/alimentos/catalogo` | Catálogo completo em JSON (ordenado por ID) |
| `GET` | `/alimentos/catalogo/csv` | Catálogo completo em CSV |
| `GET` | `/alimentos/catalogo/mysql` | Script MySQL para carregar o catálogo no PratoFácil |
| `POST` | `/calculo` | Totais nutricionais de uma lista de alimentos + porções |

As rotas `GET` mandam `Cache-Control` (1 h na busca, 1 dia no catálogo),
porque os dados só mudam quando a TACO é reimportada.

### Busca: `GET /alimentos?nome=feijao carioca&limite=2`

Cada palavra digitada precisa ser o começo de uma palavra do nome, então a
busca funciona enquanto a pessoa digita (`feij` já encontra `Feijão`).
Primeiro vêm os nomes que começam com o texto buscado; depois, os mais curtos.

```json
[
  {
    "idAlimento": 561,
    "nomeAlimento": "Feijão, carioca, cozido",
    "categoria": "Leguminosas e derivados",
    "por100g": {
      "kcal": 76.0,
      "carboidratoG": 13.6,
      "proteinaG": 4.8,
      "gorduraG": 0.5,
      "sodioMg": 2.0
    }
  }
]
```

Para uma porção de *Q* gramas: `valor = por100g.campo × Q ÷ 100`.

### Detalhe: `GET /alimentos/561`

Os mesmos campos da busca, mais:

```json
"micros": [
  { "nome": "Cálcio", "valorPor100g": 27.00, "unidade": "mg" },
  { "nome": "Sódio",  "valorPor100g": 2.00,  "unidade": "mg" }
]
```

### Cálculo: `POST /calculo`

```json
{ "itens": [ { "idAlimento": 1, "quantidadeGramas": 150 } ] }
```

Resposta (o contrato da v1.0.0 foi mantido; `unidadesMicros` é novo):

```json
{
  "totalCalorias": 186.0,
  "macros": { "proteinas": 3.9, "carboidratos": 38.7, "gorduras": 1.5 },
  "micros": { "Sódio": 1.5, "Cálcio": 7.5 },
  "unidadesMicros": { "Cálcio": "mg", "Sódio": "mg" }
}
```

Regras: `itens` com 1 a 100 elementos; `idAlimento` obrigatório e maior que 0;
`quantidadeGramas` obrigatória, maior que 0 e no máximo 10000.

## Campos e unidades

| Campo | Unidade | Observação |
|---|---|---|
| `por100g.kcal` | kcal | por 100 g |
| `por100g.carboidratoG` | g | por 100 g |
| `por100g.proteinaG` | g | por 100 g |
| `por100g.gorduraG` | g | lipídeos totais, por 100 g |
| `por100g.sodioMg` | mg | por 100 g |
| `micros[].valorPor100g` | `micros[].unidade` | mg para todos, menos Vitamina A (mcg) |
| `totalCalorias` | kcal | total da refeição |
| `macros.*` | g | total da refeição |
| `micros.*` | ver `unidadesMicros` | total da refeição |

- Valores por 100 g têm 1 casa decimal (micronutrientes no detalhe, 2).
- `null` = nutriente **não analisado** na TACO (`NA`). Não é zero.
- Traços (`Tr` na TACO) aparecem como `0.0`.
- `idAlimento` é o ID desta API, **não** o ID do catálogo do PratoFácil.

## Erros

Todos os erros têm o mesmo formato:

```json
{
  "status": 400,
  "mensagem": "Requisição inválida: verifique os campos informados.",
  "timestamp": "2026-11-26T17:30:00",
  "erros": [
    { "campo": "itens[0].quantidadeGramas", "mensagem": "a quantidadeGramas deve ser maior que zero" }
  ]
}
```

| Status | Quando |
|---|---|
| 400 | Parâmetro ausente, em branco ou fora da faixa; JSON inválido; campo inválido no cálculo |
| 403 | Chamada de navegador vinda de uma origem que não está em `APINUTRI_CORS_ORIGENS` |
| 404 | Alimento ou rota inexistente |
| 500 | Erro inesperado (detalhes só no log do servidor) |

## Carga do catálogo no PratoFácil (MySQL)

O jeito mais simples é usar o script gerado pela própria API:

1. Acorde a API (a primeira chamada no Render pode levar 30–50 s) e baixe o script:
   ```bash
   curl -o carga-catalogo-taco-pratofacil.sql https://api-nutri-uu2q.onrender.com/alimentos/catalogo/mysql
   ```
   (ou abra o link no navegador: o arquivo é baixado.)
2. Faça um backup do banco do PratoFácil.
3. Rode o arquivo no MySQL do projeto (MySQL Workbench → *File → Run SQL Script*,
   ou `mysql -u usuario -p nome_do_banco < carga-catalogo-taco-pratofacil.sql`).

O script:

- cria as 15 categorias da TACO na tabela `categoria` (as que já existirem com o
  mesmo nome são aproveitadas);
- insere os 597 alimentos na tabela `alimento` com `id_categoria`, `nome`,
  `porcao_desc = '100 g'`, `porcao_g = 100`, `kcal_100g`, `carb_100g`,
  `prot_100g`, `gord_100g` e `sodio_100g_mg` (1 casa decimal, cabe em
  `DECIMAL(6,1)` / `DECIMAL(7,1)`); `codigo_barras` fica `NULL`;
- roda em uma transação e pode ser executado de novo sem duplicar nada
  (alimento com o mesmo nome não é inserido de novo);
- usa `NULL` para nutriente não analisado — se alguma coluna do PratoFácil for
  `NOT NULL`, troque por `0` ou ajuste a coluna antes.
## Variáveis de ambiente

| Variável | Padrão | Para que serve |
|---|---|---|
| `SPRING_DATASOURCE_URL` | — | URL JDBC do PostgreSQL (ex.: Neon) |
| `SPRING_DATASOURCE_USERNAME` | — | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | — | Senha do banco |
| `APINUTRI_CORS_ORIGENS` | `http://localhost:5173` | Origens liberadas no navegador, separadas por vírgula. Ex.: `https://pbe-ads-2026-2-p3-tidai-5899101.onrender.com,http://localhost:5173` |
| `APINUTRI_IMPORTADOR_HABILITADO` | `true` | Importa a TACO (tabela vazia) ou completa as categorias (tabela populada) ao subir |
| `PORT` | `8080` | Porta HTTP (o Render define sozinho) |

Chamadas de servidor para servidor (ex.: o back-end Node.js do PratoFácil) não
mandam o cabeçalho `Origin` e não são afetadas pelo CORS.

## Como rodar localmente

1. Clone o repositório
2. Crie um banco PostgreSQL vazio e rode `database/schema.sql`
   (cria as tabelas e o catálogo de nutrientes)
3. Copie `src/main/resources/application.yml.example` para
   `src/main/resources/application.yaml` e preencha suas credenciais
4. Execute `ApiNutriApplication`
5. Acesse `http://localhost:8080/swagger-ui/index.html`

> Na primeira execução, os dados da Tabela TACO são importados
> automaticamente a partir de `src/main/resources/data/alimentos.csv`.

### Rodando com Docker

```bash
docker build -t api-nutri .
docker run -p 8080:8080 --env-file .env api-nutri
```

## Testes

```bash
mvn test
```

Os testes usam um banco H2 em memória (configurado em
`src/test/resources/application.properties`), então não precisam de PostgreSQL.

| Classe | O que cobre |
|---|---|
| `CalculoNutricionalServiceTest` | Cálculo proporcional, soma de vários alimentos, **gorduras ("Lipídeos")**, unidades dos micros |
| `AlimentoServiceTest` | Busca sem acento, várias palavras, ordem de relevância, limite, validação, valores por 100 g, `null` para NA, cache |
| `TextoBuscaTest` | Normalização do texto da busca |
| `CatalogoExportServiceTest` | CSV e script MySQL (colunas, `NULL`, aspas, sem duplicar) |
| `TacoImportServiceTest` | Completar categorias no banco já publicado |
| `ApiIntegracaoTest` | Aplicação inteira com o CSV real: rotas, erros 400/404, exportações, CORS |

## Arquitetura

- **Stateless** — a API não armazena refeições nem dados de usuário.
  Autenticação e persistência de refeições ficam a cargo da aplicação
  consumidora.
- **Em camadas** — `Controller → Service → Repository`, com DTOs isolando o
  contrato da API das entidades JPA.
- **Modelo normalizado** — tabelas associativas para as relações N:N entre
  alimentos e nutrientes; categorias em tabela própria.
- **Catálogo em memória** — os 597 alimentos são lidos do banco uma vez
  (3 consultas) e a busca roda em memória, o que a deixa rápida e independente
  de extensões do PostgreSQL para ignorar acentos.
- **Mudanças de banco versionadas** — `ddl-auto: validate`; toda alteração de
  tabela fica em `database/migracoes/`.

## Tecnologias

| Categoria | Stack |
|---|---|
| Linguagem / Runtime | Java 21 |
| Framework | Spring Boot (Web, Data JPA, Validation) |
| Banco de dados | PostgreSQL (H2 nos testes) |
| Documentação | Springdoc OpenAPI (Swagger) |
| Produtividade | Lombok |
| Testes | JUnit 5, Mockito, MockMvc |
| Deploy | Docker · Render · Neon |

## Fonte dos dados

Dados nutricionais baseados na **Tabela Brasileira de Composição de
Alimentos (TACO)**, desenvolvida pelo NEPA/UNICAMP.

## Histórico de versões

**1.1.0**
- Correção: o total de gorduras do `POST /calculo` vinha sempre 0
- Categoria da TACO gravada e devolvida em cada alimento
- Busca sem acento, por palavras, com relevância e `limite`
- Alimentos devolvidos com valores por 100 g (kcal, carboidrato, proteína, gordura, sódio)
- `GET /alimentos/{id}` com micronutrientes e unidades
- Exportação do catálogo em JSON, CSV e script MySQL para o PratoFácil
- Validação com erro 400 legível; 404 em JSON para rotas inexistentes
- CORS restrito às origens configuradas
- `unidadesMicros` no retorno do cálculo
- Importador em uma transação e completando categorias de bancos antigos

**1.0.0** — busca por nome, cálculo nutricional, importação da TACO, Swagger e deploy.

**Melhorias futuras**
- [ ] Inserção em lote (batch insert) no importador, para reduzir o tempo
  de carga inicial em produção

## Autor

**Igor Pacheco** — 
[LinkedIn](https://www.linkedin.com/in/igor-pacheco1/)
