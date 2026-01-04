package com.kt.dto.tag;

import com.kt.domain.pet.PetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TagRequest {

    public record Create(
            @NotBlank  String key,
            @NotBlank  String name,
            @NotNull PetType petType
    ) {}

    public record Update(
            @NotBlank String key,
            @NotBlank String name,
            PetType petType,
            Boolean active
    ) {}
}