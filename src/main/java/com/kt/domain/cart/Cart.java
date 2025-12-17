package com.kt.domain.cart;

import java.util.ArrayList;
import java.util.List;

import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.user.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "cart")
@Entity
@Getter
@NoArgsConstructor
public class Cart extends BaseTimeEntity {

	@OneToOne(fetch = FetchType.LAZY) //지연로딩
	private User user;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartProduct> products = new ArrayList<>();

	//회원 1명당 장바구니 1개
	private Cart(User user){
		this.user = user;
	}
	public static Cart create(User user){

		return new Cart(user);
	}


}
