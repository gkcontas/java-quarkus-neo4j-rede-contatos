# Rede de Contatos

Modelagem de um grafo de relacionamentos (pessoas e conexões) em Java com Quarkus e Neo4j, expondo consultas típicas de grafo (conexões diretas, conexões de 2º grau e menor caminho) via API REST.

## Status

✅ MVP implementado.

## Stack

- Java 17 + Quarkus 3.39
- Neo4j (extensão [Quarkiverse Neo4j](https://github.com/quarkiverse/quarkus-neo4j), driver oficial do Neo4j)
- Lombok (na classe de domínio `Person`)
- Gradle (Kotlin DSL) + wrapper `gradlew`
- Quarkus Dev Services (Neo4j automático em dev/test, via Testcontainers) + JUnit 5 + RestAssured

## Por que Neo4j faz sentido aqui

Consultas como "amigos de amigos" ou "menor caminho entre duas pessoas" são baratas e naturais em um banco de grafos (percorrer relacionamentos é O(1) por salto, independente do tamanho total do grafo), mas ficam caras e verbosas em SQL relacional (exigem múltiplos `JOIN`s recursivos ou CTEs recursivas). O Cypher expressa esse tipo de consulta quase da mesma forma que se desenha o grafo no papel.

## Grafo de exemplo (usado nos testes)

```
Alice — Bob — Carol — Dave
          \
           Erin
```

- Conexões diretas de `Alice`: `Bob`
- Conexões de 2º grau de `Alice` (amigos de amigos, excluindo diretos): `Carol`, `Erin`
- Menor caminho de `Alice` até `Dave`: `Alice → Bob → Carol → Dave` (3 saltos)

## Como rodar

**Opção 1 — modo dev (Dev Services sobe Neo4j automaticamente, requer Docker):**
```bash
./gradlew quarkusDev
```

**Opção 2 — com docker-compose explícito:**
```bash
docker compose up -d
./gradlew quarkusDev
```

A API sobe em `http://localhost:8080`. O navegador do Neo4j (quando usado o docker-compose) fica em `http://localhost:7474` (usuário `neo4j`, senha `contact-network`).

## Como rodar os testes

```bash
./gradlew test
```

Os testes usam Quarkus Dev Services (Testcontainers por baixo) para subir um Neo4j real automaticamente — é necessário ter Docker disponível.

## Modelo de dados

- Nó `Person { id, name }` — `id` é um UUID gerado pela aplicação (não o id interno do Neo4j)
- Relacionamento `(:Person)-[:CONNECTED_TO]->(:Person)` — criado em uma direção (exigência sintática do Cypher para `MERGE`), mas toda consulta de leitura usa o padrão não-direcionado `-[:CONNECTED_TO]-`, então a conexão se comporta como bidirecional do ponto de vista da API

## Endpoints principais

| Método | Rota                                    | Descrição                                                    |
|--------|-------------------------------------------|-----------------------------------------------------------------|
| POST   | `/people`                                 | Cria uma pessoa                                                 |
| GET    | `/people/{id}`                            | Busca pessoa por id                                              |
| POST   | `/people/{id}/connections/{otherId}`      | Cria uma conexão entre duas pessoas                             |
| GET    | `/people/{id}/connections`                | Lista conexões diretas (1 salto)                                 |
| GET    | `/people/{id}/connections/second-degree`  | Lista conexões de 2º grau (2 saltos, excluindo diretas)          |
| GET    | `/people/{id}/path/{otherId}`             | Retorna o menor caminho até outra pessoa (`shortestPath` do Cypher) |

## Exemplo de uso

```bash
# Criar pessoas
curl -s -X POST localhost:8080/people -H "Content-Type: application/json" -d '{"name": "Alice"}'
curl -s -X POST localhost:8080/people -H "Content-Type: application/json" -d '{"name": "Bob"}'

# Conectar (use os ids retornados acima)
curl -s -X POST localhost:8080/people/{aliceId}/connections/{bobId}

# Conexões diretas de Alice
curl -s localhost:8080/people/{aliceId}/connections

# Conexões de 2º grau de Alice
curl -s localhost:8080/people/{aliceId}/connections/second-degree

# Menor caminho entre Alice e outra pessoa
curl -s localhost:8080/people/{aliceId}/path/{otherId}
```
