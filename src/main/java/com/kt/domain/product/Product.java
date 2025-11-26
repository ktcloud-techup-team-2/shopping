package com.kt.domain.product;

import com.kt.common.jpa.BaseAuditEntity;
import com.kt.common.api.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.kt.common.Preconditions.validate;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseAuditEntity {

	@Column(nullable = false, length = 200)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private int price;

	@Column(nullable = false)
	private int stockQuantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProductStatus status;

	private Product(
		String name,
		String description,
		int price,
		int stockQuantity
	) {
		this.name = validateName(name);
		this.description = description;
		this.price = validatePrice(price);
		this.stockQuantity = validateStock(stockQuantity);
		// 최초 등록시 항상 DRAFT(임시 저장)
		this.status = ProductStatus.DRAFT;
	}

	public static Product create(
		String name,
		String description,
		int price,
		int stockQuantity
	) {
		return new Product(name, description, price, stockQuantity);
	}

	private String validateName(String name) {
		validate(name != null && !name.isBlank(), ErrorCode.PRODUCT_NAME_REQUIRED);
		validate(name.length() <= 200, ErrorCode.PRODUCT_NAME_TOO_LONG);
		return name;
	}

	private int validatePrice(int price) {
		validate(price >= 0, ErrorCode.PRODUCT_PRICE_BELOW_MINIMUM);
		return price;
	}

	private int validateStock(int stock) {
		validate(stock >= 0, ErrorCode.PRODUCT_STOCK_BELOW_MINIMUM);
		return stock;
	}
}
