package com.kt.domain.pet;

import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "pets")
public class Pet extends BaseSoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private PetType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private boolean neutered;   // 중성화 여부

    @Column(nullable = false)
    private String breed;

    @Column(nullable = false)
    private String birthday;

    private Double weight;

    @Column(name = "body_shape")
    private BodyShape bodyShape;

    @Column
    private boolean allergy;

    @Column
    private String photoUrl;

    private Pet(
            User user,
            PetType type,
            String name,
            Gender gender,
            boolean neutered,
            String breed,
            String birthday,
            Double weight,
            BodyShape bodyShape,
            boolean allergy,
            String photoUrl
    ) {
        this.user = user;
        this.type = type;
        this.name = name;
        this.gender = gender;
        this.neutered = neutered;
        this.breed = breed;
        this.birthday = birthday;
        this.weight = weight;
        this.bodyShape = bodyShape;
        this.allergy = allergy;
        this.photoUrl = photoUrl;
    }

    public static Pet create(
            User user,
            PetType type,
            String name,
            Gender gender,
            boolean neutered,
            String breed,
            String birthday,
            Double weight,
            BodyShape bodyShape,
            boolean allergy,
            String photoUrl
    ) {
        return new Pet(
                user,
                type,
                name,
                gender,
                neutered,
                breed,
                birthday,
                weight,
                bodyShape,
                allergy,
                photoUrl
        );
    }

    public void update(
            boolean neutered,
            Double weight,
            BodyShape bodyShape,
            boolean allergy,
            String photoUrl
    ) {
        this.neutered = neutered;
        this.weight = weight;
        this.bodyShape = bodyShape;
        this.allergy = allergy;
        this.photoUrl = photoUrl;
    }

    public void delete(Long deleterId) {
        markDeleted(deleterId);
    }
}
