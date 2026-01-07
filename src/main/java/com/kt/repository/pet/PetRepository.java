package com.kt.repository.pet;

import com.kt.domain.pet.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    Page<Pet> findAllByUser_IdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<Pet> findByIdAndDeletedFalse(Long id);

    Optional<Pet> findByIdAndUser_IdAndDeletedFalse(Long petId, Long userId);
}
