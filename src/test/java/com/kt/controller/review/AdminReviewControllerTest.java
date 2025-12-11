package com.kt.controller.review;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import com.kt.domain.pet.PetType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.common.api.PageBlock;
import com.kt.domain.product.Product;
import com.kt.domain.review.Review;
import com.kt.domain.user.Role;
import com.kt.domain.user.User;
import com.kt.dto.review.ReviewResponse;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.review.ReviewRepository;
import com.kt.repository.user.UserRepository;

@Transactional
class AdminReviewControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/admin/reviews";
	private static final Long TEST_USER_ID = 100L;
	private static final Long TEST_ADMIN_ID = 1L;

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@MockitoBean
	private RedissonClient redissonClient;

	@Autowired
	private RestDocsFactory restDocsFactory;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;


	@Nested
	class 관리자_전체_리뷰_조회_API {
		@Test
		void 성공() throws Exception {
			// given
			Pageable pageable = PageRequest.of(0, 10);

			createUser(TEST_ADMIN_ID, Role.ADMIN);
			User regularUser = createUser(TEST_USER_ID, Role.USER);
			Product product = createProduct();

			Review review1 = createReview(regularUser.getId(), product.getId(), 5, "최고의 상품");
			Review review2 = createReview(regularUser.getId(), product.getId(), 4, "좋아요");

			Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2), pageable, 2);

			var responseList = reviewPage.getContent().stream()
				.map(ReviewResponse::from)
				.toList();

			var docsResponse = ApiResponse.ofPage(responseList, toPageBlock(reviewPage));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "?page=0&size=10",
							null,
							HttpMethod.GET,
							objectMapper
						)
						.with(jwtAdmin())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"admin-review-list",
						"전체 리뷰 조회",
						"관리자가 모든 리뷰를 페이징 조회합니다.",
						"Admin-Review",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	class 관리자_리뷰_강제_삭제_API {
		@Test
		void 성공() throws Exception {
			// given
			createUser(TEST_ADMIN_ID, Role.ADMIN);
			User regularUser = createUser(TEST_USER_ID, Role.USER);
			Product product = createProduct();
			Long reviewId = createReview(regularUser.getId(), product.getId(), 1, "삭제대상").getId();

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{reviewId}",
							null,
							HttpMethod.DELETE,
							objectMapper,
							reviewId
						)
						.with(jwtAdmin())
				)
				.andExpect(status().isNoContent())
				.andDo(
					restDocsFactory.success(
						"admin-review-delete",
						"리뷰 강제 삭제",
						"관리자가 부적절한 리뷰를 삭제합니다.",
						"Admin-Review",
						null,
						null
					)
				);

			assertThat(reviewRepository.findById(reviewId)).isEmpty();
		}
	}

	private User createUser(Long id, Role role) {
		User user;
		if (role == Role.ADMIN) {
			user = User.admin(
				"admin" + id,
				"pass",
				"관리자",
				"admin@kt.com",
				"010-0000-0000",
				null,
				null,
				LocalDateTime.now(),
				LocalDateTime.now());
		} else {
			user = User.user(
				"user" + id,
				"pass",
				"일반사용자",
				"user@kt.com",
				"010-1111-1111",
				null,
				null,
				LocalDateTime.now(),
				LocalDateTime.now());
		}
		return userRepository.save(user);
	}

	private Product createProduct() {
		Product product = Product.create("테스트 상품", "설명", 10000, PetType.DOG);
		return productRepository.save(product);
	}

	private Review createReview(Long userId, Long productId, Integer rating, String content) {
		Review review = Review.create(userId, productId, rating, content, null);
		return reviewRepository.save(review);
	}

	private PageBlock toPageBlock(Page<?> page) {
		return new PageBlock(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext(),
			page.hasPrevious(),
			page.getSort().stream()
				.map(order -> new PageBlock.SortOrder(order.getProperty(), order.getDirection().name()))
				.toList()
		);
	}
}