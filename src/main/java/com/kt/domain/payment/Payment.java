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
	private Long orderAmount;

	@Column(nullable = false)
	private Long deliveryFee = 2500L; //배송비에 대한 로직 필요 ex)얼마이상무료,제주산간지역

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
		Order order,
		Long deliveryFee,
		String orderNumber,
		Long orderAmount,
		PaymentType type
	){
		this.userId = userId;
		this.order = order;
		this.deliveryFee = deliveryFee;
		this.orderNumber = orderNumber;
		this.orderAmount = orderAmount;
		this.type = type;
		this.paymentAmount = deliveryFee+orderAmount;
		this.status = PaymentStatus.READY;
	}

	//진행
	public static Payment create(
		Long userId,
		Order order,
		Long deliveryFee,
		String orderNumber,
		Long orderAmount,
		PaymentType type
	){
		return new Payment (userId,order,deliveryFee,orderNumber,orderAmount,type);
	}
}
