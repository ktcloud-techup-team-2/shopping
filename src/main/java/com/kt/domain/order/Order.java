package com.kt.domain.order;

import java.util.UUID;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Table;

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

	@Column(nullable = false)
	private Long userId;

	@Column(unique = true, nullable = false)
	private String orderNumber;

	//주문자가 볼 수 있는 주문번호 추가

	@Embedded
	private Receiver receiver;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private long totalPaymentAmount;

	@OneToMany(mappedBy = "order")
	private List<OrderProduct> orderProducts = new ArrayList<>();

	private Order(Long userId,Receiver receiver, long totalPaymentAmount) {
		this.userId = userId;
		this.receiver = receiver;
		this.totalPaymentAmount = totalPaymentAmount;
		this.orderStatus = OrderStatus.PENDING;
		this.orderNumber = UUID.randomUUID().toString();
	}

	public static Order create(Long userId, Receiver receiver, long totalPaymentAmount) {
		return new Order(userId, receiver, totalPaymentAmount);
	}

	public void mapToOrder(OrderProduct orderProduct) {
		orderProducts.add(orderProduct);
	}

}