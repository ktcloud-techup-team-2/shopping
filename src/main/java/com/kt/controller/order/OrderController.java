package com.kt.controller.order;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.order.Order;
import com.kt.dto.order.OrderRequest;
import com.kt.dto.order.OrderResponse;
import com.kt.security.AuthUser;
import com.kt.service.order.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	// 장바구니 주문
	@PostMapping("/cart")
	public ApiResponseEntity<OrderResponse.Create> createCartOrder(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestBody @Valid OrderRequest.CartOrder request
	) {
		Order order = orderService.createCartOrder(authUser.id(), request);
		return ApiResponseEntity.created(OrderResponse.Create.from(order));
	}

	// 바로 주문
	@PostMapping("/direct")
	public ApiResponseEntity<OrderResponse.Create> createDirectOrder(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestBody @Valid OrderRequest.DirectOrder request
	) {
		Order order = orderService.createDirectOrder(authUser.id(), request);
		return ApiResponseEntity.created(OrderResponse.Create.from(order));
	}

	// 주문 결제 요청 (결제하기 버튼 클릭)
	/*
	@PostMapping("/payment/start")
	public ApiResponseEntity<OrderResponse.PaymentReady> startPayment(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestBody @Valid OrderRequest.StartPayment request
	) {
		OrderResponse.PaymentReady response = orderService.startPayment(authUser.id(), request);
		return ApiResponseEntity.success(response);
	}*/

	// 사용자 1명이 주문한 목록들
	@GetMapping
	public ApiResponseEntity<List<OrderResponse.OrderList>> myOrderList(
		@AuthenticationPrincipal AuthUser authUser
	) {
		List<Order> orders = orderService.myOrderList(authUser.id());
		List<OrderResponse.OrderList> response = orders.stream()
			.map(order -> OrderResponse.OrderList.from(order))
			.toList();
		return ApiResponseEntity.success(response);
	}

	// 주문에 대한 자세한 정보(상세조회)
	@GetMapping("/{orderNumber}")
	public ApiResponseEntity<OrderResponse.MyOrder> myOrderInfo(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable String orderNumber
	) {
		Order order = orderService.myOrderInfo(orderNumber, authUser.id());
		return ApiResponseEntity.success(OrderResponse.MyOrder.from(order));
	}

	// 주문 취소
	@PatchMapping("/{orderNumber}/cancel")
	public ApiResponseEntity<OrderResponse.MyOrder> cancelOrder(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable String orderNumber
	) {
		Order order = orderService.cancelOrder(authUser.id(), orderNumber);
		return ApiResponseEntity.success(OrderResponse.MyOrder.from(order));
	}

	// 수정
	@PatchMapping("/{orderNumber}")
	public ApiResponseEntity<OrderResponse.MyOrder> updateOrder(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable String orderNumber,
		@RequestBody @Valid OrderRequest.Update request
	) {
		Order order = orderService.updateOrder(authUser.id(), orderNumber, request);
		return ApiResponseEntity.success(OrderResponse.MyOrder.from(order));
	}

}
