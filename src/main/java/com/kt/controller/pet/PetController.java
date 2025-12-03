package com.kt.controller.pet;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.pet.PetRequest;
import com.kt.dto.pet.PetResponse;
import com.kt.security.AuthUser;
import com.kt.service.pet.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/my/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ApiResponseEntity<PetResponse.Create> createPet(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody PetRequest.Create request) {
        PetResponse.Create response = petService.create(authUser.id(), request);
        return ApiResponseEntity.created(response);
    }

    @PatchMapping("/{id}")
    public ApiResponseEntity<PetResponse.Update> updatePet (@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody PetRequest.Update request) {
        PetResponse.Update response = petService.update(id, authUser.id(), request);
        return ApiResponseEntity.success(response);
    }
}
