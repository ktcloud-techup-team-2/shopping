package com.kt.dto.product;

import com.kt.domain.pet.PetType;

public record ProductRecommendRow(
        Long productId,
        String name,
        int price,
        String status,
        PetType petType,
        Long matchedTagCount
) {}
