package com.gkcontas.network.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 * Builds a small known graph and exercises direct connections, second-degree
 * connections and shortest path:
 *
 * <pre>
 *   Alice - Bob - Carol - Dave
 *             \
 *              Erin
 * </pre>
 */
@QuarkusTest
class ContactNetworkIntegrationTest {

    private String createPerson(String name) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"%s\"}".formatted(name))
                .when().post("/people")
                .then().statusCode(201)
                .extract().path("id");
    }

    private void connect(String id, String otherId) {
        given().when().post("/people/{id}/connections/{otherId}", id, otherId)
                .then().statusCode(204);
    }

    @Test
    void shouldFindDirectAndSecondDegreeConnectionsAndShortestPath() {
        String alice = createPerson("Alice");
        String bob = createPerson("Bob");
        String carol = createPerson("Carol");
        String dave = createPerson("Dave");
        String erin = createPerson("Erin");

        connect(alice, bob);
        connect(bob, carol);
        connect(bob, erin);
        connect(carol, dave);

        given().when().get("/people/{id}/connections", alice)
                .then().statusCode(200)
                .body("name", contains("Bob"));

        given().when().get("/people/{id}/connections/second-degree", alice)
                .then().statusCode(200)
                .body("name", containsInAnyOrder("Carol", "Erin"));

        given().when().get("/people/{id}/path/{otherId}", alice, dave)
                .then().statusCode(200)
                .body("hops", is(3))
                .body("people.name", contains("Alice", "Bob", "Carol", "Dave"));
    }

    @Test
    void shouldReturnEmptyConnectionsForAnIsolatedPerson() {
        String isolated = createPerson("Isolated");

        given().when().get("/people/{id}/connections", isolated)
                .then().statusCode(200)
                .body("$", empty());
    }

    @Test
    void shouldReturnNotFoundForMissingPerson() {
        given().when().get("/people/{id}", "does-not-exist")
                .then().statusCode(404);
    }

    @Test
    void shouldRejectConnectingAPersonToThemselves() {
        String person = createPerson("Solo");

        given().when().post("/people/{id}/connections/{otherId}", person, person)
                .then().statusCode(400);
    }

    @Test
    void shouldReturnBadRequestForBlankName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"\"}")
                .when().post("/people")
                .then().statusCode(400);
    }
}
