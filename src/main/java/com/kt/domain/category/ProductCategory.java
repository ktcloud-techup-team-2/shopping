package com.kt.domain.category;

import static com.kt.common.Preconditions.*;

import com.kt.common.api.ErrorCode;
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
		nullValidate(product, ErrorCode.COMMON_INVALID_ARGUMENT);
		nullValidate(category, ErrorCode.COMMON_INVALID_ARGUMENT);
		return new ProductCategory(product, category);
	}
}