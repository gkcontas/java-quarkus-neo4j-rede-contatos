package com.gkcontas.network.exception;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(String id) {
        super("Person not found with id %s".formatted(id));
    }
}
