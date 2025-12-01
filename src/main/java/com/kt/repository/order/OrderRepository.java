package com.kt.repository.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.order.Order;

public interface OrderRepository extends JpaRepository<Order,Long>{

	//로그인id로 모든 주문을 조회
	List<Order> findByUserId(Long userId);

	//주문넘버랑 유저id로 특정 주문을 조회
	Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);

}
