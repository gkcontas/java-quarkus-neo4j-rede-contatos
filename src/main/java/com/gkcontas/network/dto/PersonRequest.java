package com.gkcontas.network.dto;

import jakarta.validation.constraints.NotBlank;

public record PersonRequest(

        @NotBlank(message = "name is required")
        String name
) {
}
