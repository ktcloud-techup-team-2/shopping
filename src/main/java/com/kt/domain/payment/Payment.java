package com.kt.domain.payment;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.order.Order;

import jakarta.persistence.*;
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
	private Long userId;

	@Column(nullable = false)
	private String orderNumber;

	@Column(nullable = false)
	private Long orderAmount;

	@Column(nullable = false)
	private Long deliveryFee; //배송비 정책 로직 추가 (ex. 금액별 무료배송, 지역별 추가요금)

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

	private Payment(Long userId, Order order, Long deliveryFee, PaymentType type) {
		this.userId = userId;
		this.order = order;
		this.orderNumber = order.getOrderNumber();
		this.orderAmount = order.getTotalPaymentAmount();
		this.deliveryFee = deliveryFee;
		this.paymentAmount = this.orderAmount + deliveryFee;
		this.type = type;
		this.status = PaymentStatus.READY;
	}

	public static Payment create(Long userId, Order order, Long deliveryFee, PaymentType type) {
		return new Payment(userId, order, deliveryFee, type);
	}

	public void approve() {
		if (!canApprove()) {
			throw new CustomException(ErrorCode.PAYMENT_APPROVE_NOT_ALLOWED);
		}
		this.status = PaymentStatus.DONE;
	}

	public void cancel() {
		if (this.status == PaymentStatus.CANCELED) {
			throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
		}
		if (!canCancel()) {
			throw new CustomException(ErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
		}
		this.status = PaymentStatus.CANCELED;
	}

	private boolean canApprove() {
		return this.status == PaymentStatus.READY || this.status == PaymentStatus.IN_PROGRESS;
	}

	private boolean canCancel() {
		return this.status == PaymentStatus.READY || this.status == PaymentStatus.DONE;
	}

	public void changeStatus(PaymentStatus status) {
		this.status = status;
	}
}
