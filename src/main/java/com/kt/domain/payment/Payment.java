package com.kt.domain.payment;

import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.order.Order;

import jakarta.persistence.*;

import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payments")
public class Payment extends BaseTimeEntity {


	//PG사 결제키
	//PG사 주문 ID

	@Column(nullable = false)
	private long userId;

	@Column(nullable = false)
	private String orderNumber;

	@Column(nullable = false)
	private Long orderAmount; //order에서 가져온 값

	@Column(nullable = false)
	private Long deliveryFee;

	//Long로 바꾸기
	//order의 paymentamount 이름 바꾸기
	@Column(nullable = false)
	private Long paymentAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	public Payment (
		Long userId,
		PaymentType type,
		Order order,
		String orderNumber,
		Long orderAmount,
		Long deliveryFee
	){
		this.userId = userId;
		this.type = type;
		this.order = order;
		this.orderNumber = orderNumber;
		this.orderAmount = orderAmount;
		this.deliveryFee = deliveryFee;
		this.paymentAmount = deliveryFee+orderAmount;
		this.status = PaymentStatus.READY;
	}
}
