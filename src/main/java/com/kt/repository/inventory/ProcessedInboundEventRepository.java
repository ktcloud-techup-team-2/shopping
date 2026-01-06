package com.kt.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kt.domain.inventory.ProcessedInboundEvent;

public interface ProcessedInboundEventRepository extends JpaRepository<ProcessedInboundEvent, Long> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		value = "INSERT IGNORE INTO processed_inbound_events (event_id, created_at, updated_at) " +
			"VALUES (:eventId, NOW(), NOW())",
		nativeQuery = true
	)
	int insertIfAbsent(@Param("eventId") String eventId);
}
