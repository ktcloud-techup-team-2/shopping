package com.kt.service.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.domain.product.Product;
import com.kt.dto.product.ProductRequest;
import com.kt.dto.product.ProductResponse;
import com.kt.repository.product.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminProductService {

	private final ProductRepository productRepository;

	public ProductResponse.Create create(ProductRequest.Create request) {
		var product = Product.create(
			request.name(),
			request.description(),
			request.price(),
			request.stockQuantity()
		);
		var saved = productRepository.save(product);
		return new ProductResponse.Create(saved.getId());
	}
}
