package com.kt.dto.tag;

import com.kt.domain.pet.PetType;
import com.kt.domain.tag.Tag;

public class TagResponse {
    public record Detail(
            Long id,
            String key,
            String name,
            PetType petType,
            boolean active
    ) {
        public static Detail from(Tag tag) {
            return new Detail(tag.getId(), tag.getKey(), tag.getName(), tag.getPetType(), tag.isActive());
        }
    }
}
