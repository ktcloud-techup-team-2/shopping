package com.kt.service.cart;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.cart.Cart;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.inventory.Inventory;
import com.kt.dto.cart.CartRequest;
import com.kt.dto.cart.CartResponse;
import com.kt.repository.cart.CartProductRepository;
import com.kt.repository.cart.CartProductRepositoryImpl;
import com.kt.repository.cart.CartRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.user.UserRepository;

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
	private final CartProductRepositoryImpl cartProductRepositoryImpl;
	private final InventoryRepository inventoryRepository;

	//장바구니 상품 추가/생성
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

		// 장바구니에 담으려는 총 수량 계산
		int totalQuantity = request.count();
		if (existCartProduct != null) {
			totalQuantity += existCartProduct.getCount();
		}

		// 실제 재고 검증
		validateStock(product.getId(), totalQuantity);

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

	//장바구니 조회(장바구니에 담긴 상품 리스트 조회)
	public List<CartResponse.Detail> detail(Long id){

		List<CartResponse.Detail> cartDetailList = new ArrayList<>();

		var cart = cartRepository.findByUserId(id).orElse(null);

		if(cart == null){
			return cartDetailList; //장바구니에 아무것도 없으면 빈 리스트 반환
		}

		//카트에 담긴 상품들을 찾아서 저장
		cartDetailList = cartProductRepositoryImpl.findCartDetailList(cart.getId());

		return cartDetailList;
	}

	//장바구니 상품 삭제
	public void delete(Long cartProductId, Long userId){

		CartProduct cartProduct = cartProductRepository.findByCartIdAndCart_UserId(cartProductId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CART_PRODUCT_NOT_FOUND));

		cartProductRepository.delete(cartProduct);

	}

	//장바구니 상품 수량 변경
	public CartResponse.CountUpdate countUpdate(Long id, CartRequest.CountUpdate request, Long cartProductId){

		CartProduct cartProduct = cartProductRepository.findByCartIdAndCart_UserId(cartProductId, id)
			.orElseThrow(() -> new CustomException(ErrorCode.CART_PRODUCT_NOT_FOUND));

		// 변경하려는 수량에 대해 실제 재고 검증
		validateStock(cartProduct.getProduct().getId(), request.count());

		cartProduct.countUpdate(request.count());

		return new CartResponse.CountUpdate(
			cartProduct.getProduct().getId(),
			cartProduct.getProduct().getName(),
			cartProduct.getCount(),
			cartProduct.getUpdatedAt()
		);
	}

	// 실제 재고 검증
	private void validateStock(Long productId, int requestedQuantity) {
		Inventory inventory = inventoryRepository.findByProductId(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));

		// 실제 재고가 1 미만이면 장바구니에 담을 수 없음
		if (inventory.getPhysicalStockTotal() < 1) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}

		// 요청 수량이 실제 재고보다 많으면 장바구니에 담을 수 없음
		if (requestedQuantity > inventory.getPhysicalStockTotal()) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}
	}
}
