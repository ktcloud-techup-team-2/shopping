package com.kt.controller.pet;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.pet.PetRequest;
import com.kt.dto.pet.PetResponse;
import com.kt.security.AuthUser;
import com.kt.service.pet.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ApiResponseEntity<PetResponse> createPet(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody PetRequest.Create request) {
        PetResponse response = petService.create(authUser.id(), request);
        return ApiResponseEntity.created(response);
    }

    @PatchMapping("/{id}")
    public ApiResponseEntity<PetResponse> updatePet (@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody PetRequest.Update request) {
        PetResponse response = petService.update(id, authUser.id(), request);
        return ApiResponseEntity.success(response);
    }

    @GetMapping
    public ApiResponseEntity<List<PetResponse>> getAllPets(@AuthenticationPrincipal AuthUser authUser,
                                                           @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponseEntity.pageOf(petService.getPet(authUser.id(), pageable));
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<PetResponse> getPetById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        PetResponse response = petService.getMyPet(authUser.id(), id);
        return ApiResponseEntity.success(response);
    }
}

