package com.kt.controller.pet;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.pet.PetRecommendTagResponse;
import com.kt.service.pet.PetRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/my/pets")
public class PetRecommendationController {

    private final PetRecommendationService petRecommendationService;

    @GetMapping("/{petId}/recommend-tags")
    public ApiResponseEntity<PetRecommendTagResponse.Result> recommendTags(@PathVariable Long petId) {
        return ApiResponseEntity.success(petRecommendationService.recommendTags(petId));
    }
}
