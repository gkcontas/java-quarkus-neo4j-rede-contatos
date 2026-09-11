package com.gkcontas.network.model;

import lombok.Value;

/**
 * Read model for a {@code (:Person)} node. Neo4j itself has no schema, so
 * this class only mirrors the {@code id}/{@code name} properties read back
 * from Cypher query results — there is no ORM mapping involved.
 */
@Value
public class Person {
    String id;
    String name;
}
