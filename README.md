# API Nutricional

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-336791?logo=postgresql&logoColor=white)
![Status](https://img.shields.io/badge/status-concluído-blue)

API REST em Spring Boot que fornece dados nutricionais (calorias, macro e
micronutrientes) de alimentos com base na **Tabela TACO (UNICAMP)** e calcula
os totais nutricionais proporcionais a uma quantidade informada.

Desenvolvida como parte de um projeto acadêmico (aplicação de montagem de
refeições) e como projeto de portfólio.

**API em produção:** [api-nutri-uu2q.onrender.com](https://api-nutri-uu2q.onrender.com) ·
**Documentação (Swagger):** [/swagger-ui/index.html](https://api-nutri-uu2q.onrender.com/swagger-ui/index.html)

---

## Sumário

- [Sobre](#sobre)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Endpoints principais](#endpoints-principais)
- [Como rodar localmente](#como-rodar-localmente)
- [Fonte dos dados](#fonte-dos-dados)
- [Status do projeto](#status-do-projeto)
- [Autor](#autor)

## Sobre

A API Nutricional resolve um problema comum de apps de nutrição e montagem de
refeições: dado um conjunto de alimentos e suas porções (em gramas), calcular
o total de calorias, macronutrientes e micronutrientes da refeição — sem que
a aplicação cliente precise carregar ou manter essa base de dados por conta
própria.

## Funcionalidades

-  Busca de alimentos por nome
-  Cálculo de calorias, macronutrientes e micronutrientes de uma refeição,
  a partir de uma lista de alimentos e suas porções em gramas

## Arquitetura

- **Stateless** — a API não armazena refeições nem dados de usuário: recebe
  alimentos + porções e devolve os nutrientes calculados na hora.
  Autenticação e persistência de refeições ficam a cargo da aplicação
  consumidora.
- **Em camadas** — `Controller → Service → Repository`, com DTOs isolando o
  contrato da API das entidades JPA.
- **Modelo de dados normalizado** — tabelas associativas para as relações
  N:N entre alimentos e nutrientes.

## Tecnologias

| Categoria | Stack |
|---|---|
| Linguagem / Runtime | Java 21 |
| Framework | Spring Boot (Web, Data JPA, Validation) |
| Banco de dados | PostgreSQL |
| Documentação | Springdoc OpenAPI (Swagger) |
| Produtividade | Lombok |
| Testes | JUnit + Mockito |
| Deploy | Docker · Render |

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/alimentos?nome=` | Busca alimentos por nome |
| `POST` | `/calculo` | Calcula nutrientes totais de uma lista de alimentos + porções |

A especificação completa, com todos os campos de request/response, fica
disponível interativamente no Swagger UI (link acima).

## Como rodar localmente

1. Clone o repositório
2. Crie um banco PostgreSQL e rode os scripts em `database/schema.sql`
   (cria as tabelas e popula o catálogo de nutrientes)
3. Copie `application.yml.example` para `application.yml` e preencha suas
   credenciais locais
4. Execute `ApiNutriApplication`
5. Acesse a documentação interativa em
   `http://localhost:8080/swagger-ui/index.html`

> Na primeira execução, os dados da Tabela TACO são importados
> automaticamente a partir de `src/main/resources/data/alimentos.csv`.

### Rodando com Docker

```bash
docker build -t api-nutri .
docker run -p 8080:8080 --env-file .env api-nutri
```

## Fonte dos dados

Dados nutricionais baseados na **Tabela Brasileira de Composição de
Alimentos (TACO)**, desenvolvida pelo NEPA/UNICAMP.

>  O serviço em produção está no plano gratuito do Render, que "dorme"
> após período de inatividade. A primeira requisição depois de um tempo
> parado pode levar de 30 a 50 segundos para responder — isso é esperado.

## Status do projeto

Concluído: modelagem de dados, importação de dados reais (TACO),
endpoints de busca e cálculo, testes automatizados, documentação e deploy
em produção.

**Melhorias futuras**
- [ ] Inserção em lote (batch insert) no importador, para reduzir o tempo
  de carga inicial em produção

## Autor

**Igor Pacheco** — 
[LinkedIn](https://www.linkedin.com/in/igor-pacheco1/)
