package com.kt.controller.user;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.order.Order;
import com.kt.dto.order.OrderResponse;
import com.kt.dto.review.ReviewResponse;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.security.AuthUser;
import com.kt.service.order.OrderService;
import com.kt.service.review.ReviewService;
import com.kt.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrderService orderService;
    private final ReviewService reviewService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseEntity<Void> signUp (@RequestBody @Valid UserRequest.Create request){
        userService.signup(request);
        return ApiResponseEntity.created((Void) null);
    }

    @GetMapping("/my-info")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseEntity<UserResponse> getInfo(@AuthenticationPrincipal AuthUser authUser){
        UserResponse response = userService.getUser(authUser.id());
        return ApiResponseEntity.success(response);
    }

    @PatchMapping("/my-info")
    public ApiResponseEntity<UserResponse> updateInfo (@AuthenticationPrincipal AuthUser authUser,
                                                       @RequestBody @Valid UserRequest.Update request){
        UserResponse response = userService.updateUser(authUser.id(), request);
        return ApiResponseEntity.success(response);
    }

    @DeleteMapping("/withdrawal")
    public ApiResponseEntity<Void> deleteMyInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.deleteUser(authUser.id());
        return ApiResponseEntity.empty();
    }

    @PatchMapping("/change-password")
    @GetMapping ("/my/orders")
    public ApiResponseEntity<List<OrderResponse.OrderList>> getMyOrders(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Long userId = authUser.id();

        List<Order> orders = orderService.myOrderList(userId);
        List<OrderResponse.OrderList> response = orders.stream()
                .map(OrderResponse.OrderList::from)
                .toList();

        return ApiResponseEntity.success(response);
    }

    @GetMapping("/my/orders/{orderNumber}")
    public ApiResponseEntity<OrderResponse.MyOrder> getMyOrderDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("orderNumber") String orderNumber
    ) {
        Long userId = authUser.id();
        Order order = orderService.myOrderInfo(orderNumber, userId);
        return ApiResponseEntity.success(OrderResponse.MyOrder.from(order));
    }

    @GetMapping("my/reviews")
    public ApiResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal AuthUser authUser, @PageableDefault(size=10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponseEntity.pageOf(
                reviewService.getReviewsByUser(authUser.id(), pageable));
    }

    @PatchMapping("/change-password")
    public ApiResponseEntity<Void> changePassword (@AuthenticationPrincipal AuthUser authUser,
                                                   @RequestBody @Valid UserRequest.PasswordChange request){
        userService.changePassword(authUser.id(), request);
        return ApiResponseEntity.empty();
    }
}
