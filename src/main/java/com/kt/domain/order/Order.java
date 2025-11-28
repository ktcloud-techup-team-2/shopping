package com.kt.domain.order;

import java.util.ArrayList;
import java.util.List;

import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.orderproduct.OrderProduct;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor
public class Order extends BaseTimeEntity {

	private Long userId;

	@Column(nullable = false, unique = true)
	private String orderNumber;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private long finalPaymentAmount;

	@OneToMany(mappedBy = "order")
	private List<OrderProduct> orderProducts = new ArrayList<>();

	public void addOrderProduct(OrderProduct orderProduct) {
		orderProducts.add(orderProduct);
	}

	private Order(

		Long userId,
		String orderNumber,
		long finalPaymentAmount

	) {
		this.userId = userId;
		this.orderNumber = orderNumber;
		this.finalPaymentAmount = finalPaymentAmount;
		this.orderStatus = OrderStatus.PENDING;
	}

	public static Order create(Long userId, String orderNumber,
		long finalPaymentAmount) {
		return new Order(userId, orderNumber, finalPaymentAmount);
	}
}