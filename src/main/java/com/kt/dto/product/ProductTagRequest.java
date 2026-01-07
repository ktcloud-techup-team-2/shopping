package com.kt.dto.product;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductTagRequest(
        @NotNull
        List<Long> tagIds
) {}
