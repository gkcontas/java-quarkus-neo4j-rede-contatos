package com.gkcontas.network.dto;

import java.util.List;

public record PathResponse(List<PersonResponse> people, int hops) {

    public static PathResponse of(List<PersonResponse> people) {
        return new PathResponse(people, Math.max(0, people.size() - 1));
    }
}
