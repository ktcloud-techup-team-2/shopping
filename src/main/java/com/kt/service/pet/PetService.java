package com.kt.service.pet;


import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.pet.Pet;
import com.kt.domain.user.User;
import com.kt.dto.pet.PetRequest;
import com.kt.dto.pet.PetResponse;
import com.kt.repository.pet.PetRepository;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public PetResponse create (Long userId, PetRequest.Create request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Pet pet = Pet.create(
                user,
                request.type(),
                request.name(),
                request.gender(),
                request.neutered(),
                request.breed(),
                request.birthday(),
                request.weight(),
                request.bodyShape(),
                request.allergy(),
                request.photoUrl()
        );

        Pet saved = petRepository.save(pet);
        return PetResponse.from(saved);
    }

    public PetResponse update (Long petId, Long userId, PetRequest.Update request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Pet pet = petRepository.findByIdAndDeletedAtIsNull(petId)
                .orElseThrow(() -> new CustomException(ErrorCode.PET_NOT_FOUND));

        Preconditions.validate(pet.getUser().getId().equals(userId), ErrorCode.PET_NOT_FOUND);

        pet.update(
                Boolean.TRUE.equals(request.neutered()),
                request.weight(),
                request.bodyShape(),
                request.allergy(),
                request.photoUrl()
        );
        return PetResponse.from(pet);
    }

    @Transactional(readOnly = true)
    public PetResponse getMyPet(Long userId, Long petId) {
        Pet pet = petRepository.findByIdAndDeletedAtIsNull(petId)
                .orElseThrow(() -> new CustomException(ErrorCode.PET_NOT_FOUND));

        Preconditions.validate(pet.getUser().getId().equals(userId), ErrorCode.PET_NOT_FOUND);

        return PetResponse.from(pet);
    }

    @Transactional(readOnly = true)
    public Page<PetResponse> getPet(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return petRepository.findAllByUser_IdAndDeletedAtIsNull(userId, pageable)
                .map(PetResponse::from);
    }

    public void deletePet(Long userId, Long petId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Pet pet = petRepository.findByIdAndDeletedAtIsNull(petId)
                .orElseThrow(() -> new CustomException(ErrorCode.PET_NOT_FOUND));

        Preconditions.validate(pet.getUser().getId().equals(userId), ErrorCode.PET_NOT_FOUND);

        pet.delete(userId);
    }
}

