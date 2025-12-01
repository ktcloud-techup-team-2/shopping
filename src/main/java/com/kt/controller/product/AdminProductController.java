package com.kt.controller.product;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

	@GetMapping
	public ApiResponseEntity<List<ProductResponse.Summary>> getProducts(Pageable pageable) {
		var response = adminProductService.getPage(pageable);
		return ApiResponseEntity.pageOf(response);
	}

	@GetMapping("/{id}")
	public ApiResponseEntity<ProductResponse.Detail> getDetail(@PathVariable Long id) {
		var response = adminProductService.getDetail(id);
		return ApiResponseEntity.success(response);
	}

	@PutMapping("/{id}")
	public ApiResponseEntity<ProductResponse.Detail> update(
		@PathVariable Long id,
		@RequestBody @Valid ProductRequest.Update request
	) {
		var response = adminProductService.update(id, request);
		return ApiResponseEntity.success(response);
	}

	@DeleteMapping("/{id}")
	public ApiResponseEntity<Void> delete(@PathVariable Long id) {
		adminProductService.delete(id);
		return ApiResponseEntity.empty();
	}

	@PostMapping("/{id}/activate")
	public ApiResponseEntity<ProductResponse.Detail> activate(@PathVariable Long id) {
		var response = adminProductService.activate(id);
		return ApiResponseEntity.success(response);
	}

	@PostMapping("/{id}/in-activate")
	public ApiResponseEntity<ProductResponse.Detail> inactivate(@PathVariable Long id) {
		var response = adminProductService.inactivate(id);
		return ApiResponseEntity.success(response);
	}

	@PostMapping("/sold-out")
	public ApiResponseEntity<List<ProductResponse.Detail>> markSoldOut(@RequestBody @Valid ProductRequest.BulkSoldOut request) {
		var response = adminProductService.markSoldOut(request);
		return ApiResponseEntity.success(response);
	}

	@PostMapping("/{id}/toggle-sold-out")
	public ApiResponseEntity<ProductResponse.Detail> toggleSoldOut(@PathVariable Long id) {
		var response = adminProductService.toggleSoldOut(id);
		return ApiResponseEntity.success(response);
	}

}
