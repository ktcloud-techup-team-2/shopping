package com.kt.repository.cart;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.kt.domain.cartproduct.QCartProduct;
import com.kt.domain.product.QProduct;
import com.kt.dto.cart.CartResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartProductRepositoryImpl implements CartProductRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<CartResponse.Detail> findCartDetailList(Long cartId) {
		QCartProduct cartProduct = QCartProduct.cartProduct;
		QProduct product = QProduct.product;

		/**
		 * 특정 장바구니(=cartId =사용자가 조회하고자 요청한 장바구니)에 담긴 상품들을 조회할건데,
		 * 각 상품의 'id/이름/가격/담은날짜' 를 함께 조회해서
		 * CartResponse 리스트로 반환하는 쿼리dsl
		 */

		 //Projections
		 //특정 엔티티를 조회할때, 엔티티의 모든 값이 필요하지 않은 경우 필요한 값들만 조회할 수 있게 하는 기능

		List<CartResponse.Detail> results = queryFactory
			.select(Projections.fields(CartResponse.Detail.class,
				cartProduct.id.as("cartId"), //as : 별칭 지정
				product.id.as("productId"),
				product.name,
				product.price,
				cartProduct.count,
				cartProduct.createdAt,
				cartProduct.updatedAt
			))
			.join(cartProduct.product, product)
			//.join(cartProduct.cart, cart) //cart는 cart_id만 필요하므로 조인 필요x
			.from(cartProduct)
			.where(cartProduct.cart.id.eq(cartId)) //전달받은 ID랑 Cart 객체의 ID가 같은 장바구니를 조회
			.orderBy(
				cartProduct.updatedAt.desc().nullsLast(), //가장 최근에 수정한 상품을 가장 먼저 보여줌 //null이면 맨 아래로
				cartProduct.createdAt.desc() //수정된적 없는 상품은 최근에 등록한 순서대로 보여줌
			)
			.fetch(); //실제 db조회가 일어나는 단계

		return results;
	}
}