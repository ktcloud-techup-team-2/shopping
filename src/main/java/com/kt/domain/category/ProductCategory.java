package com.kt.domain.category;

import com.kt.common.jpa.BaseAuditEntity;
import com.kt.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseAuditEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	private ProductCategory(Product product, Category category) {
		this.product = product;
		this.category = category;
	}

	public static ProductCategory create(Product product, Category category) {
		if (product == null) {
			throw new IllegalArgumentException("product cannot be null");
		}
		if (category == null) {
			throw new IllegalArgumentException("category cannot be null");
		}
		return new ProductCategory(product, category);
	}
}