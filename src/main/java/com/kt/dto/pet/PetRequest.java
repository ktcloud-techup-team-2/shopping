package com.kt.dto.pet;

import com.kt.domain.pet.BodyShape;
import com.kt.domain.pet.PetType;
import com.kt.domain.user.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class PetRequest {
    public record Create(
            @NotNull(message = "반려동물 종류(type)은 필수 값입니다.")
            PetType type,
            @NotBlank(message = "이름은 필수 값입니다.")
            String name,
            @NotNull(message = "성별은 필수 값입니다.")
            Gender gender,
            Boolean neutered,
            @NotBlank(message = "품종(breed)은 필수 값입니다.")
            String breed,
            @NotBlank(message = "생일(birthday)은 필수 값입니다.")
            String birthday,
            @PositiveOrZero(message = "몸무게는 0 이상이어야 합니다.")
            Double weight,
            BodyShape bodyShape,
            boolean allergy,
            String photoUrl
    ) {}
}
