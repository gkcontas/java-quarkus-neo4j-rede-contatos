package com.gkcontas.network.repository;

import com.gkcontas.network.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;

@ApplicationScoped
public class PersonRepository {

    private final Driver driver;

    @Inject
    public PersonRepository(Driver driver) {
        this.driver = driver;
    }

    public Person create(String name) {
        String id = UUID.randomUUID().toString();
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Record record = tx.run(
                        "CREATE (p:Person {id: $id, name: $name}) RETURN p",
                        Map.of("id", id, "name", name)
                ).single();
                return toPerson(record.get("p").asNode());
            });
        }
    }

    public Optional<Person> findById(String id) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run("MATCH (p:Person {id: $id}) RETURN p", Map.of("id", id));
                return result.hasNext()
                        ? Optional.of(toPerson(result.single().get("p").asNode()))
                        : Optional.empty();
            });
        }
    }

    /**
     * Creates a CONNECTED_TO relationship from {@code id} to {@code otherId}.
     * The relationship has a direction internally (required by Cypher's
     * CREATE/MERGE syntax), but every read query below matches it with an
     * undirected pattern, so the connection behaves as bidirectional from
     * the API's point of view. MERGE avoids creating a duplicate edge if the
     * same connection is requested twice.
     */
    public void connect(String id, String otherId) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(
                    """
                    MATCH (a:Person {id: $id}), (b:Person {id: $otherId})
                    MERGE (a)-[:CONNECTED_TO]->(b)
                    """,
                    Map.of("id", id, "otherId", otherId)
            ).consume());
        }
    }

    /**
     * Direct (1-hop) connections of a person, in either relationship
     * direction.
     */
    public List<Person> findDirectConnections(String id) {
        String cypher = """
                MATCH (p:Person {id: $id})-[:CONNECTED_TO]-(other:Person)
                RETURN DISTINCT other
                ORDER BY other.name
                """;
        return runAndCollect(cypher, Map.of("id", id), "other");
    }

    /**
     * Second-degree connections ("friends of friends"): people reachable in
     * exactly two hops, excluding the person itself and anyone who is
     * already a direct connection.
     */
    public List<Person> findSecondDegreeConnections(String id) {
        String cypher = """
                MATCH (p:Person {id: $id})-[:CONNECTED_TO]-(:Person)-[:CONNECTED_TO]-(fof:Person)
                WHERE fof.id <> $id
                  AND NOT (p)-[:CONNECTED_TO]-(fof)
                RETURN DISTINCT fof
                ORDER BY fof.name
                """;
        return runAndCollect(cypher, Map.of("id", id), "fof");
    }

    /**
     * Shortest path between two people, ignoring relationship direction.
     * Returns an empty list when either person does not exist or there is
     * no path connecting them.
     */
    public List<Person> findShortestPath(String id, String otherId) {
        String cypher = """
                MATCH (a:Person {id: $id}), (b:Person {id: $otherId}),
                      path = shortestPath((a)-[:CONNECTED_TO*]-(b))
                RETURN path
                """;
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(cypher, Map.of("id", id, "otherId", otherId));
                if (!result.hasNext()) {
                    return List.of();
                }
                Path path = result.single().get("path").asPath();
                List<Person> people = new ArrayList<>();
                for (Node node : path.nodes()) {
                    people.add(toPerson(node));
                }
                return people;
            });
        }
    }

    private List<Person> runAndCollect(String cypher, Map<String, Object> params, String column) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(cypher, params);
                List<Person> people = new ArrayList<>();
                while (result.hasNext()) {
                    people.add(toPerson(result.next().get(column).asNode()));
                }
                return people;
            });
        }
    }

    private Person toPerson(Node node) {
        return new Person(node.get("id").asString(), node.get("name").asString());
    }
}
