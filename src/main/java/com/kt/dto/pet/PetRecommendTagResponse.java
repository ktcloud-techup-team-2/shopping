package com.kt.dto.pet;

import java.util.List;

public class PetRecommendTagResponse {
    public record TagItem(
            Long tagId,
            String key,
            String name
    ) {}

    public record Result (
            Long petId,
            List<TagItem> recommendedTags
    ) {}
}
