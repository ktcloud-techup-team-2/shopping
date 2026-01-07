package com.kt.dto.pet;

import com.kt.domain.pet.PetType;

import java.util.List;

public class PetRecommendProductResponse {
    public record ProductItem(
            Long productId,
            String name,
            int price,
            String status,
            PetType petType,
            Long matchedTagCount
    ) {}

    public record Result(
            Long petId,
            List<ProductItem> products
    ) {}
}
