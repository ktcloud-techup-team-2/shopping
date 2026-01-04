package com.kt.controller.wms;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.wms.InboundConfirmedRequest;
import com.kt.dto.wms.InboundConfirmedResponse;
import com.kt.service.inventory.WmsInboundIngressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wms/inbounds")
@RequiredArgsConstructor
public class WmsInboundController {

	private final WmsInboundIngressService wmsInboundIngressService;

	@PostMapping("/confirmed")
	public ApiResponseEntity<InboundConfirmedResponse> confirmInbound(
		@RequestBody @Valid InboundConfirmedRequest request
	) {
		var response = wmsInboundIngressService.confirmInbound(request);
		return ApiResponseEntity.success(response);
	}
}