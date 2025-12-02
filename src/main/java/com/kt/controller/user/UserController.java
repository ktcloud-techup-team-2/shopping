package com.kt.controller.user;

import com.kt.common.api.ApiResponseEntity;
import com.kt.domain.order.Order;
import com.kt.dto.order.OrderResponse;
import com.kt.dto.user.UserRequest;
import com.kt.dto.user.UserResponse;
import com.kt.security.AuthUser;
import com.kt.service.order.OrderService;
import com.kt.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
