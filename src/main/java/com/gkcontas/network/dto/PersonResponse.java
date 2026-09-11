package com.gkcontas.network.dto;

import com.gkcontas.network.model.Person;

public record PersonResponse(String id, String name) {

    public static PersonResponse of(Person person) {
        return new PersonResponse(person.getId(), person.getName());
    }
}
