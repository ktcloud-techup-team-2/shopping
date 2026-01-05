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

	//결제키
	private String paymentKey;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String orderNumber;

	@Column(nullable = false)
	private Long orderAmount;

	@Column(nullable = false)
	private Long deliveryFee;

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
		this.orderAmount = order.getOrderAmount();
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

	//결제 승인 확정
	//외부 PG사(토스)로부터 받은 결제 키를 저장하고 상태를 DONE으로 변경
	public void confirmPayment(String paymentKey) {
		this.paymentKey = paymentKey;
		this.status = PaymentStatus.DONE; // 상태를 '결제 완료'로 변경
	}

	//결제 실패
	public void failPayment() {
		this.status = PaymentStatus.FAILED;
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

	public boolean isReady() {
		return this.status == PaymentStatus.READY;
	}

	public void changeStatus(PaymentStatus status) {
		this.status = status;
	}

}
