package com.kt.controller.order;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.order.Order;
import com.kt.dto.order.OrderRequest;
import com.kt.dto.order.OrderResponse;
import com.kt.service.order.AdminOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

	private final AdminOrderService adminOrderService;

	// 주문 목록 조회
	@GetMapping
	public ApiResponseEntity<List<OrderResponse.AdminList>> allOrders() {
		List<Order> orders = adminOrderService.allOrders();
		List<OrderResponse.AdminList> response = orders.stream()
			.map(order -> OrderResponse.AdminList.from(order))
			.toList();
		return ApiResponseEntity.success(response);
	}

	// 상세 조회
	@GetMapping("/{id}")
	public ApiResponseEntity<OrderResponse.AdminDetail> orderInfo(@PathVariable Long id) {
		Order order = adminOrderService.orderInfo(id);
		return ApiResponseEntity.success(OrderResponse.AdminDetail.from(order));
	}

	// 관리자 주문 취소
	@PatchMapping("/{id}/cancel")
	public ApiResponseEntity<OrderResponse.AdminDetail> cancelOrderAdmin(@PathVariable Long id) {
		Order order = adminOrderService.cancelOrderAdmin(id);
		return ApiResponseEntity.success(OrderResponse.AdminDetail.from(order));
	}

	// 관리자 주문 상태 변경
	@PatchMapping("/{id}/change-status")
	public ApiResponseEntity<OrderResponse.AdminDetail> changeStatus(
		@PathVariable Long id,
		@RequestBody @Valid OrderRequest.ChangeStatus request
	) {
		Order order = adminOrderService.changeStatus(id, request);
		return ApiResponseEntity.success(OrderResponse.AdminDetail.from(order));
	}
}

