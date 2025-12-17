package com.kt.controller.cart;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.cart.CartRequest;
import com.kt.dto.cart.CartResponse;
import com.kt.security.AuthUser;
import com.kt.service.cart.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	@PostMapping("/addProduct")
	public ApiResponseEntity<CartResponse.Create> create(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestBody @Valid CartRequest.Add request
	){
		var response = cartService.create(request, authUser.id());

		return ApiResponseEntity.created(response);
	}

	@GetMapping
	public ApiResponseEntity<List<CartResponse.Detail>> detail(
		@AuthenticationPrincipal AuthUser authUser
	){
		var response = cartService.detail(authUser.id());

		return ApiResponseEntity.success(response);
	}
}
