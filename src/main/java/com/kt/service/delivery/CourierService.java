package com.kt.service.delivery;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.Preconditions;
import com.kt.domain.delivery.Courier;
import com.kt.dto.delivery.CourierRequest;
import com.kt.dto.delivery.CourierResponse;
import com.kt.repository.delivery.CourierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CourierService {
	private final CourierRepository courierRepository;

	public CourierResponse createCourier(CourierRequest.Create request) {

		boolean isDuplicated = courierRepository.existsByCode(request.code());
		Preconditions.validate(!isDuplicated, ErrorCode.COURIER_CODE_DUPLICATED);

		Courier courier = Courier.create(request.code(), request.name());
		Courier savedCourier = courierRepository.save(courier);

		return CourierResponse.from(savedCourier);
	}

	public List<CourierResponse> getCourierList() {
		return courierRepository.findAll().stream()
			.map(CourierResponse::from)
			.toList();
	}

	public CourierResponse updateCourier(Long courierId, CourierRequest.Update request) {
		Courier courier = findCourierById(courierId);

		courier.update(request.name(), request.isActive());

		return CourierResponse.from(courier);
	}

	public void deleteCourier(Long courierId) {
		Courier courier = findCourierById(courierId);
		courierRepository.delete(courier);
	}


	private Courier findCourierById(Long courierId) {
		return courierRepository.findById(courierId)
			.orElseThrow(() -> new CustomException(ErrorCode.COURIER_NOT_FOUND));
	}
}