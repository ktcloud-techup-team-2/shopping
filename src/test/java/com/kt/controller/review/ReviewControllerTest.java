package com.kt.controller.review;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import com.kt.domain.pet.PetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.kt.dto.review.ReviewRequest;
import com.kt.dto.review.ReviewResponse;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.review.ReviewRepository;
import com.kt.repository.user.UserRepository;

@Transactional
class ReviewControllerTest extends AbstractRestDocsTest {

	private static final String DEFAULT_URL = "/reviews";
	private static final Long TEST_USER_ID = 1L;

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
	@DisplayName("리뷰 작성 API")
	class CreateReview {
		@Test
		void 성공() throws Exception {
			// given
			Product product = createProduct();

			var request = new ReviewRequest.Create(product.getId(), 5, "좋아요", null);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL,
							request,
							HttpMethod.POST,
							objectMapper
						)
						.with(jwtUser())
						.with(csrf())
				)
				.andExpect(status().isCreated())
				.andDo(result -> {
					var response = objectMapper.readValue(result.getResponse().getContentAsString(), ApiResponse.class);
					var responseData = (LinkedHashMap) response.getData();
					Long reviewId = Long.valueOf(responseData.get("reviewId").toString());

					Review createdReview = reviewRepository.findById(reviewId).orElseThrow();
					var docsResponse = ApiResponse.of(ReviewResponse.from(createdReview));

					restDocsFactory.success(
						"review-create",
						"리뷰 작성",
						"상품에 대한 리뷰를 작성합니다.",
						"Review",
						request,
						docsResponse
					).handle(result);
				});
		}
	}

	@Nested
	@DisplayName("상품별 리뷰 목록 조회 API")
	class GetReviewListByProduct {
		@Test
		void 성공() throws Exception {
			// given
			User user = createUser(TEST_USER_ID, Role.USER);
			Product product = createProduct();
			Long productId = product.getId();

			createReview(user.getId(), productId, 5, "최고");
			createReview(user.getId(), productId, 4, "좋아요");

			Pageable pageable = PageRequest.of(0, 10);
			Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

			var responseList = reviewPage.getContent().stream()
				.map(ReviewResponse::from)
				.toList();

			var docsResponse = ApiResponse.ofPage(responseList, toPageBlock(reviewPage));

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL,
							null,
							HttpMethod.GET,
							objectMapper
						)
						.param("productId", String.valueOf(productId))
						.with(jwtUser())
				)
				.andExpect(status().isOk())
				.andDo(
					restDocsFactory.success(
						"review-list-by-product",
						"상품 리뷰 조회",
						"특정 상품의 리뷰 목록을 페이징 조회합니다.",
						"Review",
						null,
						docsResponse
					)
				);
		}
	}

	@Nested
	@DisplayName("리뷰 수정 API")
	class UpdateReview {
		@Test
		void 성공() throws Exception {
			// given
			User user = createUser(TEST_USER_ID, Role.USER);
			Product product = createProduct();
			Long reviewId = createReview(user.getId(), product.getId(), 5, "수정 전 내용").getId();

			var request = new ReviewRequest.Update(4, "수정함", null);

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{reviewId}",
							request,
							HttpMethod.PUT,
							objectMapper,
							reviewId
						)
						.with(jwtUser(user.getId()))
						.with(csrf())
				)
				.andExpect(status().isOk())
				.andDo(result -> {
					Review updatedReview = reviewRepository.findById(reviewId).orElseThrow();
					var docsResponse = ApiResponse.of(ReviewResponse.from(updatedReview));

					restDocsFactory.success(
						"review-update",
						"리뷰 수정",
						"자신이 작성한 리뷰를 수정합니다.",
						"Review",
						request,
						docsResponse
					).handle(result);
				});
		}
	}

	@Nested
	@DisplayName("리뷰 삭제 API")
	class DeleteReview {
		@Test
		void 성공() throws Exception {
			// given
			User user = createUser(TEST_USER_ID, Role.USER);
			Product product = createProduct();
			Long reviewId = createReview(user.getId(), product.getId(), 5, "삭제 대상").getId();

			// when & then
			mockMvc.perform(
					restDocsFactory.createRequest(
							DEFAULT_URL + "/{reviewId}",
							null,
							HttpMethod.DELETE,
							objectMapper,
							reviewId
						)
						.with(jwtUser(user.getId()))
						.with(csrf())
				)
				.andExpect(status().isNoContent())
				.andDo(
					restDocsFactory.success(
						"review-delete",
						"리뷰 삭제",
						"자신이 작성한 리뷰를 삭제합니다.",
						"Review",
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