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

	private Order(Long userId, Receiver receiver, Long orderAmount, String orderNumber) {
		this.userId = userId;
		this.receiver = receiver;
		this.orderAmount = orderAmount;
		this.orderStatus = OrderStatus.PENDING;
		this.orderNumber = orderNumber;
	}

	public static Order create(Long userId, Receiver receiver, Long orderAmount, String orderNumber) {
		return new Order(userId, receiver, orderAmount, orderNumber);
	}

	public void mapToOrder(OrderProduct orderProduct) {
		orderProducts.add(orderProduct);
	}

	public void cancel() {

		//이미 취소되어 있으면
		if(alreadyCanceled()){
			throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
		}

		//취소할 수 없으면
		if (!canCancelUser()) {
			throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
		}
		this.orderStatus = OrderStatus.CANCELLED;
	}

	public void cancelAdmin(){

		if(alreadyCanceled()){
			throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
		}
		this.orderStatus = OrderStatus.CANCELLED;
	}

	private boolean canCancelUser(){
		return this.orderStatus == OrderStatus.PENDING || this.orderStatus == OrderStatus.COMPLETED;
	}
	//이미 취소한 주문인지 확인
	private boolean alreadyCanceled(){
		return this.orderStatus == OrderStatus.CANCELLED;
	}

	//주문 수정 = 배송 정보 수정
	public void updateReceiver(Receiver receiver){
		this.receiver = receiver;
	}

	public void changeOrderStatus(OrderStatus status) {
		this.orderStatus = status;
	}

}