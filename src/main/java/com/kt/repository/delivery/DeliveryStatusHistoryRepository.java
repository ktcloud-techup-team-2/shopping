package com.kt.repository.delivery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.delivery.DeliveryStatusHistory;

public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, Long> {
	List<DeliveryStatusHistory> findAllByDeliveryIdOrderByIdDesc(Long deliverId);
}
