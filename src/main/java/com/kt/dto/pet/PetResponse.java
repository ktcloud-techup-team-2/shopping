package com.kt.dto.pet;

import com.kt.domain.pet.Pet;

public class PetResponse {

    public record Create (
            Long id
    ) {
        public static Create from (Pet pet) {
            return new Create(pet.getId());
        }
    }
}
