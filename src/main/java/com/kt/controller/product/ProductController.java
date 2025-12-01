package com.kt.controller.product;

import java.util.List;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.product.ProductResponse;
import com.kt.service.product.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductQueryService productQueryService;

	@GetMapping
	public ApiResponseEntity<List<ProductResponse.Summary>> getProducts(Pageable pageable) {
		var response = productQueryService.getPublicProducts(pageable);
		return ApiResponseEntity.pageOf(response);
	}

	@GetMapping("/{id}")
	public ApiResponseEntity<ProductResponse.Detail> getDetail(@PathVariable Long id) {
		var response = productQueryService.getPublicProduct(id);
		return ApiResponseEntity.success(response);
	}
}