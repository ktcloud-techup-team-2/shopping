package com.kt.controller.user;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.order.Order;
import com.kt.domain.order.Receiver;
import com.kt.domain.review.Review;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.order.OrderResponse;
import com.kt.dto.review.ReviewResponse;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.review.ReviewRepository;
import com.kt.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class UserControllerTest extends AbstractRestDocsTest {

    private static final String SIGNUP_URL = "/users/signup";
    private static final String INFO_URL = "/users/my-info";
    private static final String WITHDRAWAL_URL = "/users/withdrawal";

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    private Long currentUserId;
    private String orderNumber1;
    private String orderNumber2;

    @BeforeEach
    void setUpUser() {
        userRepository.deleteAll();

        User user = User.user(
                "test1234",
                "encoded-password",
                "테스트유저",
                "example123@gmail.com",
                "010-1234-5678",
                LocalDate.of(2000, 8, 9),
                Gender.FEMALE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        currentUserId = userRepository.save(user).getId();

        Receiver receiver = new Receiver(
                "테스트수령인",
                "서울시 강남구 테헤란로 123",
                "010-1111-2222"
        );

        Order order1 = Order.create(
                currentUserId,
                receiver,
                10000L,
                "ORD-TEST-001"
        );
        Order order2 = Order.create(
                currentUserId,
                receiver,
                20000L,
                "ORD-TEST-002"
        );

        orderRepository.saveAll(List.of(order1, order2));
        orderNumber1 = "ORD-TEST-001";
        orderNumber2 = "ORD-TEST-002";

        Review review1 = Review.create(
                currentUserId,
                1L,
                5,
                "아주 만족스러운 상품입니다.",
                "http://image-server/review1.png"
        );

        Review review2 = Review.create(
                currentUserId,
                2L,
                4,
                "무난하게 쓸만해요.",
                null
        );

        reviewRepository.saveAll(List.of(review1, review2));
    }


    @Nested
    class 회원가입_API {

        @Test
        void 성공 () throws Exception {
            // given
            UserRequest.Create request = new UserRequest.Create(
                    "idfortest123",
                    "PasswordTest123!",
                    "PasswordTest123!",
                    "JNSJ",
                    "example123@example.com",
                    "010-1234-1234",
                    Gender.MALE,
                    LocalDate.of(1999, 9, 9)
            );

            // then -> body 없이 201 created
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            SIGNUP_URL,
                            request,
                            HttpMethod.POST,
                            objectMapper
                    )
            )
                    .andExpect(status().isCreated())
                    .andDo(
                            restDocsFactory.success(
                                    "user-signup",
                                    "회원가입",
                                    "신규 회원을 생성하는 API",
                                    "User",
                                    request,
                                    null
                            )
                    );
        }
    }

    @Nested
    class 유저_정보_조회_API {
        @Test
        void 성공 () throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    INFO_URL,
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(currentUserId))
                    .andExpect(jsonPath("$.data.loginId").value("test1234"))
                    .andExpect(jsonPath("$.data.name").value("테스트유저"))
                    .andExpect(jsonPath("$.data.email").value("example123@gmail.com"))
                    .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                    .andDo(
                            restDocsFactory.success(
                                    "users-me",
                                    "내 정보 조회",
                                    "현재 로그인한 사용자의 정보를 조회하는 API",
                                    "User",
                                    null,
                                    UserResponse.class
                            )
                    );
        }
    }

    @Nested
    class 유저_정보_수정_API {
        @Test
        void 성공() throws Exception {
            UserRequest.Update request = new UserRequest.Update(
                    "수정된이름",
                    "updated@example.com",
                    "010-9999-9999",
                    LocalDate.of(2000, 1, 1)
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    INFO_URL,
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(currentUserId))
                    .andExpect(jsonPath("$.data.loginId").value("test1234"))
                    .andExpect(jsonPath("$.data.name").value(request.name()))
                    .andExpect(jsonPath("$.data.email").value(request.email()))
                    .andExpect(jsonPath("$.data.phone").value(request.phone()))
                    .andExpect(jsonPath("$.data.birthday").value("2000-01-01"))
                    .andDo(
                            restDocsFactory.success(
                                    "users-me-update",
                                    "내 정보 수정",
                                    "현재 로그인한 사용자의 정보를 수정하는 API",
                                    "User",
                                    request,
                                    UserResponse.class
                            )
                    );
        }

        @Test
        void 실패_인증_없음 () throws Exception {
            // given
            UserRequest.Update request = new UserRequest.Update(
                    "수정된이름",
                    "updated@example.com",
                    "010-9999-9999",
                    LocalDate.of(2000, 1, 1)
            );

            // when & then (인증 토큰 없이 호출)
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    INFO_URL,
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper
                            )
                    )
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class 유저_탈퇴_API {

        @Test
        void 성공 () throws Exception {
            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    WITHDRAWAL_URL,
                                    null,
                                    HttpMethod.DELETE,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isNoContent())
                    .andDo(
                            restDocsFactory.success(
                                    "users-me-delete",
                                    "내 정보 탈퇴",
                                    "현재 로그인한 사용자의 계정을 탈퇴(soft delete)하는 API",
                                    "User",
                                    null,
                                    null
                            )
                    );

        }
    }

    @Nested
    class 내_주문_목록_조회_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    "/users/my/orders",
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].orderNumber").value(orderNumber1))
                    .andDo(
                            restDocsFactory.success(
                                    "users-my-orders",
                                    "내 주문 목록 조회",
                                    "현재 로그인한 사용자의 주문 목록을 조회하는 API",
                                    "User-Order",
                                    null,
                                    OrderResponse.OrderList[].class
                            )
                    );
        }
    }

    @Nested
    class 내_주문_상세_조회_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    "/users/my/orders/" + orderNumber1,
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderNumber").value(orderNumber1))
                    .andDo(
                            restDocsFactory.success(
                                    "users-my-orders-detail",
                                    "내 주문 상세 조회",
                                    "현재 로그인한 사용자의 특정 주문 상세 정보를 조회하는 API",
                                    "User-Order",
                                    null,
                                    OrderResponse.MyOrder.class
                            )
                    );
        }
    }

    @Nested
    class 내_리뷰_목록_조회_API {

        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    "/users/my/reviews",
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    // 아래 필드 이름은 ReviewResponse 구조에 맞게 필요하면 수정
                    .andExpect(jsonPath("$.data[0].rating").value(4))
                    .andExpect(jsonPath("$.data[0].content").value("무난하게 쓸만해요."))
                    .andDo(
                            restDocsFactory.success(
                                    "users-my-reviews",
                                    "내 리뷰 목록 조회",
                                    "현재 로그인한 사용자의 리뷰 목록을 조회하는 API",
                                    "User-Review",
                                    null,
                                    ReviewResponse[].class
                            )
                    );
        }
    }

}
