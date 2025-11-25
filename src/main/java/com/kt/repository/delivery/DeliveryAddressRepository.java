package com.kt.repository.delivery;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.delivery.DeliveryAddress;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
	List<DeliveryAddress> findByUserIdAndIsActiveTrue(Long userId);

	Optional<DeliveryAddress> findByUserIdAndIsDefaultTrueAndIsActiveTrue(Long userId);

	Optional<DeliveryAddress> findByIdAndUserId(Long id, Long userId);

	int countByUserIdAndIsActiveTrue(Long userId);
}
