package com.kt.repository.user;

import com.kt.domain.user.Role;
import com.kt.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT user FROM User user WHERE user.loginId = :loginId")
    Optional<User> findByLoginId(@Param("loginId") String loginId);

    boolean existsByloginId(String loginId);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    List<User> findAllByDeletedAtIsNull();

    List<User> findAllByRoleAndDeletedAtIsNull(Role role);
}
