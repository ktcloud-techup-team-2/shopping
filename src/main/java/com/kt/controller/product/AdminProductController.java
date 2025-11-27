package com.kt.controller.product;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.kt.service.product.AdminProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
	private final AdminProductService adminProductService;

	@PostMapping
	public ApiResponseEntity<ProductResponse.Create> create(@RequestBody @Valid ProductRequest.Create request) {
		var response = adminProductService.create(request);
		return ApiResponseEntity.created(response);
	}
}
