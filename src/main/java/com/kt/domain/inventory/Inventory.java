package com.kt.domain.inventory;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseAuditEntity;
import com.kt.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "inventories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseAuditEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false, unique = true)
	private Product product;

	/**
	 * WMS(입고/출고 확정) 기준 실제 물리적으로 존재하는 총 재고량
	 * inbound 시 증가, outbound 확정 시 감소
	 */
	@Column(nullable = false)
	private long physicalStockTotal;

	/**
	 * OMS 기준 예약 재고 (장바구니/주문 생성 시점)
	 * 재고를 소비하기 전 단계에서 '예약' 상태로 묶음
	 */
	@Column(nullable = false)
	private long reserved;

	/**
	 * OMS Commit(출고 요청) 이후 WMS 확정 전까지의 재고
	 * “출고 처리 중” 상태
	 * OMS → WMS Confirm 사이의 중간 영역
	 */
	@Column(nullable = false)
	private long outboundProcessing;

	/**
	 * 실제 판매 가능 재고 = physicalStockTotal - reserved - outboundProcessing
	 * 주문 생성 시 이 값을 기준으로 결제/예약이 가능 여부를 판단함
	 */
	@Column(nullable = false)
	private long available;

	// 생성자는 항상 상품과 초기 재고 상태(0)로 시작
	private Inventory(Product product) {
		this.product = product;
		this.physicalStockTotal = 0L;
		this.reserved = 0L;
		this.outboundProcessing = 0L;
		recalculateAvailability();
	}

	/**
	 * 상품 생성 시, Inventory 엔티티 생성 팩토리 메서드
	 */
	public static Inventory initialize(Product product) {
		return new Inventory(product);
	}

	/**
	 * WMS 입고 완료 처리
	 * 물리 재고가 증가하며 주문 가능 재고도 증가
	 */
	public void applyWmsInbound(long quantity) {
		ensurePositive(quantity, ErrorCode.INVENTORY_EVENT_QUANTITY_INVALID);
		this.physicalStockTotal += quantity;
		recalculateAvailability();
	}

	/**
	 * WMS 출고 확정 처리
	 * - OMS outboundProcessing, physicalStockTotal 실제 재고 감소
	 */
	public void applyWmsOutboundConfirmed(long quantity) {
		ensurePositive(quantity, ErrorCode.INVENTORY_EVENT_QUANTITY_INVALID);

		// 출고 처리 중 수량보다 많은 출고 확정 불가
		Preconditions.validate(outboundProcessing >= quantity, ErrorCode.INVENTORY_OUTBOUND_NOT_RESERVED);

		// 물리 재고도 충분해야 함
		Preconditions.validate(physicalStockTotal >= quantity, ErrorCode.INVENTORY_NEGATIVE_AVAILABLE);

		this.outboundProcessing -= quantity;
		this.physicalStockTotal -= quantity;

		recalculateAvailability();
	}

	/**
	 * WMS 출고 취소 처리
	 * - OMS에서 commit 했지만 WMS에서 취소된 경우
	 * - 다시 주문 가능 재고로 돌아감
	 */
	public void applyWmsOutboundCanceled(long quantity) {
		ensurePositive(quantity, ErrorCode.INVENTORY_EVENT_QUANTITY_INVALID);

		Preconditions.validate(outboundProcessing >= quantity, ErrorCode.INVENTORY_OUTBOUND_NOT_RESERVED);

		this.outboundProcessing -= quantity;

		recalculateAvailability();
	}

	/**
	 * OMS 예약 발생 (장바구니 / 주문 생성 단계)
	 * - available(판매 가능 재고) 기반으로 예약
	 * - 예약하면 reserved → 증가
	 */
	public void applyOmsReserve(long quantity) {
		ensurePositive(quantity, ErrorCode.INVENTORY_EVENT_QUANTITY_INVALID);

		// 판매 가능 재고보다 많이 예약할 수 없음
		Preconditions.validate(available >= quantity, ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);

		this.reserved += quantity;

		recalculateAvailability();
	}

	/**
	 * OMS 예약 해제 (주문 취소 / 타임아웃 / 그 외)
	 * - 예약 해제되면 reserved 감소
	 */
	public void applyOmsRelease(long quantity) {
		ensurePositive(quantity, ErrorCode.INVENTORY_EVENT_QUANTITY_INVALID);

		Preconditions.validate(reserved >= quantity, ErrorCode.INVENTORY_RESERVATION_NOT_FOUND);

		this.reserved -= quantity;

		recalculateAvailability();
	}

	/**
	 * OMS Commit 단계
	 * - 사용자가 결제 성공 → OMS에서 출고 요청 상태로 변경
	 * - reserved → 감소
	 * - outboundProcessing → 증가
	 *
	 * 즉, “주문 확정, 물류 출고 요청된 상태”
	 */
	public void applyOmsCommit(long quantity) {
		ensurePositive(quantity, ErrorCode.INVENTORY_EVENT_QUANTITY_INVALID);

		Preconditions.validate(reserved >= quantity, ErrorCode.INVENTORY_RESERVATION_NOT_FOUND);

		this.reserved -= quantity;
		this.outboundProcessing += quantity;

		recalculateAvailability();
	}

	/**
	 * 판매 가능 재고 확인
	 */
	public boolean hasAvailableStock() {
		return available > 0;
	}

	/**
	 * 수량이 양수인지 검증
	 * 재고 이벤트는 대부분 양수 단위로 다루기 때문에 공통 체크
	 */
	private void ensurePositive(long quantity, ErrorCode errorCode) {
		Preconditions.validate(quantity > 0, errorCode);
	}

	/**
	 * 재고 가용량 재계산
	 * available = physical - reserved - outbound
	 *
	 * 음수가 발생하면 도메인 규칙 위반 → 즉시 예외 발생
	 */
	private void recalculateAvailability() {
		this.available = physicalStockTotal - reserved - outboundProcessing;

		if (this.available < 0) {
			throw new CustomException(ErrorCode.INVENTORY_NEGATIVE_AVAILABLE);
		}
	}
}