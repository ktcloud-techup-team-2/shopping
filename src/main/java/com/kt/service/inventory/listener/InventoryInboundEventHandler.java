package com.kt.service.inventory.listener;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.inventory.event.InboundConfirmedEvent;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.inventory.ProcessedInboundEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryInboundEventHandler {

	private final ProcessedInboundEventRepository processedInboundEventRepository;
	private final InventoryRepository inventoryRepository;
	private final RedissonClient redissonClient;

	@EventListener
	@Transactional
	public void handleInboundConfirmed(InboundConfirmedEvent event) {
		boolean firstProcessed = tryInsertEvent(event.eventId());
		if (!firstProcessed) {
			log.info("[WMS_INBOUND_DUPLICATE] eventId={}", event.eventId());
			return;
		}

		String lockKey = "lock:inventory:{" + event.productId() + "}";
		RLock lock = redissonClient.getLock(lockKey);
		if (lock == null) {
			throw new CustomException(ErrorCode.WMS_INBOUND_LOCK_ACQUIRE_FAILED);
		}

		boolean locked = false;
		try {
			locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
			if (!locked) {
				throw new CustomException(ErrorCode.WMS_INBOUND_LOCK_ACQUIRE_FAILED);
			}

			var inventory = inventoryRepository.findByProductId(event.productId())
				.orElseThrow(() -> new CustomException(ErrorCode.WMS_INBOUND_INVENTORY_NOT_FOUND));

			inventory.applyWmsInbound(event.quantity());
			inventoryRepository.save(inventory);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(ErrorCode.WMS_INBOUND_LOCK_ACQUIRE_FAILED);
		} finally {
			if (locked) {
				lock.unlock();
			}
		}
	}

	private boolean tryInsertEvent(String eventId) {
		return processedInboundEventRepository.insertIfAbsent(eventId) > 0;
	}
}
