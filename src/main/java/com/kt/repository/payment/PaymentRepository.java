package com.kt.repository.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.payment.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrderNumber(String orderNumber);

	// 결제 ID와 유저 ID가 모두 일치하는 데이터 조회
	Optional<Payment> findByIdAndUserId(Long id, Long userId);
}
