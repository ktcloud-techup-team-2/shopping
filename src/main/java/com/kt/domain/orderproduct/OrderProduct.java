package com.kt.domain.orderproduct;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.order.Order;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class OrderProduct extends BaseTimeEntity {

	@ManyToOne
	@JoinColumn(name = "order_id")
	private Order order;

	private String productName;
	private int productPrice;
	private int quantity;
	private int totalPrice;

	private OrderProduct(
		String productName,
		int productPrice,
		int quantity,
		Order order
	) {
		if (quantity < 1) {
			throw new CustomException(ErrorCode.ORDER_PRODUCT_QUANTITY_MINIMUM);
		}
		this.productName = productName;
		this.productPrice = productPrice;
		this.quantity = quantity;
		this.order = order;
		this.totalPrice = productPrice * quantity;
	}

	public static OrderProduct create(
		String productName,
		int productPrice,
		int quantity,
		Order order
	) {
		return new OrderProduct(productName, productPrice, quantity, order);
	}

}