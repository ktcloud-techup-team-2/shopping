package com.kt.controller.pet;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.pet.PetRequest;
import com.kt.dto.pet.PetResponse;
import com.kt.dto.user.UserResponse;
import com.kt.security.AuthUser;
import com.kt.service.pet.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/my/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ApiResponseEntity<PetResponse.Create> create(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody PetRequest.Create request) {
        PetResponse.Create petId = petService.create(authUser.id(), request);
        return ApiResponseEntity.created(petId);
    }
}
