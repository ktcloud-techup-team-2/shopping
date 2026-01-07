package com.kt.controller.product;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.product.ProductTagRequest;
import com.kt.dto.product.ProductTagResponse;
import com.kt.service.product.ProductTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminProductTagController {

    private final ProductTagService productTagService;

    @PutMapping("/{productId}/tags")
    public ApiResponseEntity<ProductTagResponse.ReplaceResult> replaceTag(@PathVariable Long productId, @Valid @RequestBody ProductTagRequest request) {
        var response = productTagService.replaceTags(productId, request);
        return ApiResponseEntity.success(response);
    }
}
