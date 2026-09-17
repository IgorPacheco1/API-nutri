# API Nutricional

API REST em Spring Boot que fornece dados nutricionais (calorias, macro
e micronutrientes) de alimentos, com base na Tabela TACO (UNICAMP), e
calcula totais nutricionais proporcionais a uma quantidade informada.

Desenvolvida como parte de um projeto acadêmico (aplicação de montagem de
refeições) e como projeto de portfólio.

## Funcionalidades

- Busca de alimentos por nome
- Cálculo de calorias, macronutrientes e micronutrientes de uma refeição,
  a partir de uma lista de alimentos e suas porções em gramas

## Arquitetura

- **Stateless**: a API não armazena refeições nem dados de usuário —
  recebe alimentos + porções e devolve os nutrientes calculados na hora.
  Autenticação e persistência de refeições ficam a cargo da aplicação
  consumidora.
- **Camadas**: Controller → Service → Repository, com DTOs isolando o
  contrato da API das entidades JPA
- **Modelo de dados**: normalizado, com tabelas associativas para as
  relações N:N entre alimentos e nutrientes (ver diagrama abaixo, se
  quiser adicionar um)

## Tecnologias

- Java 17+, Spring Boot (Web, Data JPA)
- PostgreSQL
- Lombok
- Springdoc OpenAPI (Swagger)
- JUnit + Mockito

## Como rodar localmente

1. Clone o repositório
2. Crie um banco PostgreSQL e rode os scripts em 'database/schema.sql`
   (cria as tabelas e popula o catálogo de nutrientes)
3. Copie `application.yml.example` para `application.yml` e
   preencha suas credenciais locais
4. Execute `ApiNutriApplication`
5. Acesse a documentação interativa em
   `http://localhost:8080/swagger-ui/index.html`

Na primeira execução, os dados da Tabela TACO são importados
automaticamente a partir de `src/main/resources/data/alimentos.csv`.

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| GET | `/alimentos?nome=` | Busca alimentos por nome |
| POST | `/calculo` | Calcula nutrientes totais de uma lista de alimentos + porções |

## Fonte dos dados

Dados nutricionais baseados na Tabela Brasileira de Composição de
Alimentos (TACO), desenvolvida pelo NEPA/UNICAMP.

## 🔗 API em produção

- **Base URL**: [ https://api-nutri-uu2q.onrender.com](https://api-nutri-uu2q.onrender.com)
- **Documentação interativa (Swagger)**: [https://api-nutri-uu2q.onrender.com/swagger-ui/index.html](https://api-nutri-uu2q.onrender.com/swagger-ui/index.html)

> Nota: o serviço está no plano gratuito do Render, que "dorme" após
> período de inatividade. A primeira requisição depois de um tempo parado
> pode levar de 30 a 50 segundos para responder — isso é esperado.

## Status do projeto

Concluído: modelagem de dados, importação de dados reais (TACO),
endpoints de busca e cálculo, testes automatizados, documentação e
deploy em produção.

### Melhorias futuras
- Inserção em lote (batch insert) no importador, para reduzir o tempo de
  carga inicial em produção
