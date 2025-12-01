package com.kt.service.delivery.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kt.domain.delivery.event.DeliveryStatusEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeliveryEventListener {

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDeliveryStatusChange(DeliveryStatusEvent event) {

		switch (event.status()) {
			case SHIPPING -> sendShippingNotification(event);
			case DELIVERED -> sendDeliveryCompletedNotification(event);
			default -> log.info("[알림 생략] 상태: {}, 배송ID: {}", event.status(), event.deliveryId());
		}
	}

	private void sendShippingNotification(DeliveryStatusEvent event) {
		log.info("================ [알림 발송] ================");
		log.info("수신자: 주문자 (OrderId: {})", event.orderId());
		log.info("내용: 고객님, 상품이 발송되었습니다. 🚚");
		log.info("택배사: {}", event.courierCode());
		log.info("송장번호: {}", event.trackingNumber());
		log.info("============================================");
	}

	private void sendDeliveryCompletedNotification(DeliveryStatusEvent event) {
		log.info("================ [알림 발송] ================");
		log.info("수신자: 주문자 (OrderId: {})", event.orderId());
		log.info("내용: 고객님, 배송이 완료되었습니다. 소중한 리뷰를 남겨주세요! 🎁");
		log.info("============================================");
	}
}