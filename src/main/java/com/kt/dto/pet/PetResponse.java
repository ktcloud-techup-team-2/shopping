package com.kt.dto.pet;

import com.kt.domain.pet.BodyShape;
import com.kt.domain.pet.Pet;
import com.kt.domain.user.Gender;

public record PetResponse (
        Long id,
        String name,
        Gender gender,
        Boolean neutered,
        String breed,
        String birthday,
        Double weight,
        BodyShape bodyShape,
        boolean allergy,
        String photoUrl
) {
    public static PetResponse from (Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getGender(),
                pet.isNeutered(),
                pet.getBreed(),
                pet.getBirthday(),
                pet.getWeight(),
                pet.getBodyShape(),
                pet.isAllergy(),
                pet.getPhotoUrl()
        );
    }
}
