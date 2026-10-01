# Rede de Contatos

API de rede social mínima modelada como grafo: pessoas são nós, amizades são relacionamentos, e as consultas são as que fazem sentido nesse formato — conexões diretas, amigos de amigos e menor caminho entre duas pessoas.

Perguntas como "quem são os amigos dos meus amigos" ou "qual o caminho mais curto entre A e B" custam caro em SQL, porque exigem joins recursivos. Em um banco de grafos cada salto é uma travessia direta de relacionamento, e o Cypher descreve a consulta quase do mesmo jeito que se desenha o grafo no papel.

## Tecnologias e bibliotecas

| | |
|---|---|
| Linguagem | Java 17 |
| Framework | Quarkus 3.39 |
| Banco | Neo4j 5, via extensão Quarkiverse Neo4j (driver oficial) |
| Consultas | Cypher |
| Validação | Hibernate Validator |
| Build | Gradle Kotlin DSL (wrapper `gradlew`) |
| Testes | JUnit 5, RestAssured, Quarkus Dev Services |
| Apoio | Lombok |

## Pré-requisitos

- JDK 17 ou superior
- Docker

## Como rodar

Em modo dev, o Quarkus sobe o Neo4j sozinho pelos Dev Services:

```bash
./gradlew quarkusDev
```

Se preferir controlar a infraestrutura manualmente:

```bash
docker compose up -d && ./gradlew quarkusDev
```

A API fica em `http://localhost:8080`. Com o docker-compose, o Neo4j Browser fica em `http://localhost:7474` (usuário `neo4j`, senha `contact-network`).

## Modelo

- Nó `Person { id, name }`, com `id` em UUID gerado pela aplicação
- Relacionamento `(:Person)-[:CONNECTED_TO]->(:Person)`

O relacionamento é gravado em uma direção, mas todas as leituras usam o padrão não direcionado `-[:CONNECTED_TO]-`, então a conexão se comporta como mútua.

Grafo de exemplo:

```
Alice — Bob — Carol — Dave
          \
           Erin
```

Para `Alice`: conexão direta é `Bob`; de 2º grau são `Carol` e `Erin`; o menor caminho até `Dave` é `Alice → Bob → Carol → Dave`.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/people` | Cria uma pessoa |
| `GET` | `/people/{id}` | Busca uma pessoa |
| `POST` | `/people/{id}/connections/{otherId}` | Conecta duas pessoas |
| `GET` | `/people/{id}/connections` | Conexões diretas |
| `GET` | `/people/{id}/connections/second-degree` | Conexões de 2º grau, sem repetir as diretas |
| `GET` | `/people/{id}/path/{otherId}` | Menor caminho até outra pessoa |

## Exemplos de uso

```bash
curl -s -X POST localhost:8080/people -H "Content-Type: application/json" -d '{"name": "Alice"}'
```

```bash
curl -s -X POST localhost:8080/people -H "Content-Type: application/json" -d '{"name": "Bob"}'
```

```bash
curl -s -X POST localhost:8080/people/{aliceId}/connections/{bobId}
```

```bash
curl -s localhost:8080/people/{aliceId}/connections
```

```bash
curl -s localhost:8080/people/{aliceId}/connections/second-degree
```

```bash
curl -s localhost:8080/people/{aliceId}/path/{daveId}
```

## Testes

```bash
./gradlew test
```

Os Dev Services sobem um Neo4j real em container automaticamente.
