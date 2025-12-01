package com.kt.service.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.order.Order;
import com.kt.dto.order.OrderRequest;
import com.kt.repository.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminOrderService {

	private final OrderRepository orderRepository;

	public List<Order> allOrders() {
		return orderRepository.findAll();
	}

	public Order orderInfo(Long id) {
		return orderRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
	}

	// 주문 취소
	public Order cancelOrderAdmin(Long id) {
		Order order = orderRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
		order.cancelAdmin();

		return order;
	}

	// 주문 상태 변경
	public Order changeStatus(Long id, OrderRequest.ChangeStatus request) {
		Order order = orderRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
		order.changeOrderStatus(request.status());
		return order;
	}
}

