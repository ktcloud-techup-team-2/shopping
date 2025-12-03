package com.kt.service.pet;


import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.pet.Pet;
import com.kt.domain.user.User;
import com.kt.dto.pet.PetRequest;
import com.kt.dto.pet.PetResponse;
import com.kt.repository.pet.PetRepository;
import com.kt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public PetResponse.Create create (Long userId, PetRequest.Create request) {
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
        return PetResponse.Create.from(saved);
    }
}

