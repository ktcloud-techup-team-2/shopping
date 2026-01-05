package com.kt.repository.auth;

import com.kt.domain.auth.OAuthAccount;
import com.kt.domain.auth.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<OAuthAccount> findByUserIdAndDeletedFalse(Long userId);
}
