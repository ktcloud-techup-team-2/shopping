package com.kt.dto.pet;

import com.kt.domain.pet.BodyShape;
import com.kt.domain.pet.Pet;
import com.kt.domain.user.Gender;

public class PetResponse {

    public record Create (
            Long id
    ) {
        public static Create from (Pet pet) {
            return new Create(pet.getId());
        }
    }

    public record Update (
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
        public static Update from (Pet pet) {
            return new Update(
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
}
