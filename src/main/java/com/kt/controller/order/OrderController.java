package com.kt.controller.order;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.order.Order;
import com.kt.dto.order.OrderRequest;
import com.kt.dto.order.OrderResponse;
import com.kt.service.order.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	//currentUser

	@PostMapping
	public ApiResponseEntity<OrderResponse.Create> create(@RequestBody @Valid OrderRequest.Create request, Long userId){
		Order order = orderService.createOrder(userId, request);
		return ApiResponseEntity.created(OrderResponse.Create.from(order));
	}

	//사용자 1명이 주문한 목록들
	@GetMapping
	public ApiResponseEntity<List<OrderResponse.OrderList>> myOrderList(@RequestParam Long userId) {
		List<Order> orders = orderService.myOrderList(userId);
		List<OrderResponse.OrderList> response = orders.stream()
			.map(order -> OrderResponse.OrderList.from(order))
			.toList();
		return ApiResponseEntity.success(response);
	}

	//주문에 대한 자세한 정보(상세조회)
	@GetMapping("/{orderNumber}")
	public ApiResponseEntity<OrderResponse.MyOrder> myOrderInfo(
		@RequestParam Long userId,
		@PathVariable String orderNumber
	) {
		Order order = orderService.myOrderInfo(orderNumber, userId);
		return ApiResponseEntity.success(OrderResponse.MyOrder.from(order));
	}

	//주문 취소
	@PatchMapping("/{orderNumber}/cancel")
	public ApiResponseEntity<OrderResponse.MyOrder> cancelOrder(
		@RequestParam Long userId,
		@PathVariable String orderNumber
	) {
		Order order = orderService.cancelOrder(userId, orderNumber);
		return ApiResponseEntity.success(OrderResponse.MyOrder.from(order));
	}

	//수정
	@PatchMapping("/{orderNumber}")
	public ApiResponseEntity<OrderResponse.MyOrder> updateOrder(
		@RequestParam Long userId,
		@PathVariable String orderNumber,
		@RequestBody @Valid OrderRequest.Update request
	) {
		Order order = orderService.updateOrder(userId, orderNumber, request);
		return ApiResponseEntity.success(OrderResponse.MyOrder.from(order));
	}


}