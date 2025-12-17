package com.kt.service.cart;

import static com.kt.domain.user.QUser.*;

import org.springframework.stereotype.Service;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.cart.Cart;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.user.User;
import com.kt.dto.cart.CartRequest;
import com.kt.dto.cart.CartResponse;
import com.kt.repository.cart.CartProductRepository;
import com.kt.repository.cart.CartRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.user.UserRepository;
import com.kt.security.AuthUser;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

	private final CartRepository cartRepository;
	private final CartProductRepository cartProductRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	//장바구니에 상품을 담는 로직
	public CartResponse.Create create(CartRequest.Add request, Long id){

		//상품
		var product = productRepository.findById(request.productId())
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
		//회원
		var user = userRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		//유저에게 장바구니가 없으면 null 반환
		var cart = cartRepository.findByUserId(user.getId()).orElse(null);

		//null이면 cart 생성
		if(cart == null){
			cart = Cart.create(user);
			cartRepository.save(cart);
		}

		//카트에 상품이 담겨 있는지 확인하고 없으면 null
		var existCartProduct = cartProductRepository.findByCartIdAndProductId(cart.getId(),product.getId())
			.orElse(null);

		CartProduct finalCartProduct;

		//null이 아니면 = 장바구니에 상품이 있으면, 수량 증가
		if(existCartProduct != null){

			existCartProduct.add(request.count());
			finalCartProduct = existCartProduct;

		}else{

			//없으면 cartProduct 상품 생성하고 저장
			var cartProduct = CartProduct.create(cart, product, request.count());
			cartProductRepository.save(cartProduct);

			finalCartProduct = cartProduct;
		}

		return CartResponse.Create.from(finalCartProduct);

	}
}
