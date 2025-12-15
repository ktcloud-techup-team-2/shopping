package com.kt.domain.orderproduct;

import static com.kt.common.Preconditions.*;


import jakarta.persistence.Table;

import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.order.Order;
import com.kt.domain.product.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "order_product")
public class OrderProduct extends BaseTimeEntity {

	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false)
	private int productPrice;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private int orderPrice;

	private OrderProduct(
		Product product,
		int quantity,
		Order order
	) {
		validate(quantity>0, ErrorCode.ORDER_PRODUCT_QUANTITY_MINIMUM);
		this.productId = product.getId();
		this.productPrice = product.getPrice();
		this.quantity = quantity;
		this.order = order;
		this.orderPrice = this.productPrice * quantity;
	}

	public static OrderProduct create(
		Product product,
		int quantity,
		Order order
	) {
		return new OrderProduct(product, quantity, order);
	}

}