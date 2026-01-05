package com.kt.domain.order;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Table;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
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

	@Embedded
	private Receiver receiver;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private Long orderAmount;

	@OneToMany(mappedBy = "order")
	private final List<OrderProduct> orderProducts = new ArrayList<>();

	private Order(Long userId, Receiver receiver, String orderNumber) {
		this.userId = userId;
		this.receiver = receiver;
		this.orderAmount = 0L;
		this.orderStatus = OrderStatus.PENDING;
		this.orderNumber = orderNumber;
	}

	public static Order create(Long userId, Receiver receiver, String orderNumber) {
		return new Order(userId, receiver, orderNumber);
	}

	public void mapToOrder(OrderProduct orderProduct) {
		orderProducts.add(orderProduct);
	}

	// 모든 주문 상품 추가 후 한 번만 호출
	public void calculateTotalAmount() {
		this.orderAmount = orderProducts.stream()
			.mapToLong(op -> (long) op.getProductPrice() * op.getQuantity())
			.sum();
	}

	//결제 취소
	public void cancel(){

		//이미 취소되었는지
		validateAlreadyCanceled();

		//사용자는 결제 대기(PENDING) 상태일 때만 취소 가능
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
		}

		this.orderStatus = OrderStatus.CANCELLED;

	}
	public void cancelAdmin() {
		//이미 취소되었는지
		validateAlreadyCanceled();

		this.orderStatus = OrderStatus.CANCELLED;
	}
	private void validateAlreadyCanceled() {
		if (this.orderStatus == OrderStatus.CANCELLED) {
			throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
		}
	}
	//주문 수정 = 배송 정보 수정
	public void updateReceiver(Receiver receiver){

		//결제 대기 상태일때만 수정 가능
		if(this.orderStatus != OrderStatus.PENDING){
			throw new CustomException(ErrorCode.ORDER_NOT_MODIFIABLE);
		}
		this.receiver = receiver;
	}

	public void changeOrderStatus(OrderStatus status) {
		this.orderStatus = status;
	}

	// 결제 대기 상태인지 확인
	public boolean isOrderPending() {
		return this.orderStatus == OrderStatus.PENDING;
	}

	// 주문 완료 처리 (결제 승인 후)
	public void complete() {
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new CustomException(ErrorCode.ORDER_NOT_PENDING);
		}
		this.orderStatus = OrderStatus.COMPLETED;
	}

}