package com.kt.controller.pet;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.pet.PetRecommendProductResponse;
import com.kt.dto.pet.PetRecommendTagResponse;
import com.kt.security.AuthUser;
import com.kt.service.pet.PetRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/my/pets")
public class PetRecommendationController {

    private final PetRecommendationService petRecommendationService;

    @GetMapping("/{petId}/recommend-tags")
    public ApiResponseEntity<PetRecommendTagResponse.Result> recommendTags(@PathVariable Long petId) {
        return ApiResponseEntity.success(petRecommendationService.recommendTags(petId));
    }

    @GetMapping("/{petId}/recommend-products")
    public ApiResponseEntity<List<PetRecommendProductResponse.ProductItem>> recommendProducts(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long petId,
            Pageable pageable
    ) {
        Page<PetRecommendProductResponse.ProductItem> page =
                petRecommendationService.recommendProducts(authUser.id(), petId, pageable);

        return ApiResponseEntity.pageOf(page);
    }
}
