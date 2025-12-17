package com.kt.repository.user;

import com.kt.domain.user.Role;
import com.kt.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByLoginIdAndDeletedAtIsNull(@Param("loginId") String loginId);

    boolean existsByloginId(String loginId);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    List<User> findAllByDeletedAtIsNull();

    List<User> findAllByRoleAndDeletedAtIsNull(Role role);

    Optional<User> findByIdAndRoleAndDeletedAtIsNull(Long id, Role role);

    Optional<User> findByEmailAndNameAndDeletedAtIsNull(String email, String name);

    Optional<User> findByLoginIdAndEmailAndNameAndDeletedAtIsNull(String loginId, String email, String name);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
