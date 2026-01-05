package com.kt.domain.auth;

import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "oauth_account",
uniqueConstraints = {
        @UniqueConstraint(name = "uk_oauth_provider_provider_id", columnNames = {"provider", "provider_user_id"})
})
public class OAuthAccount extends BaseSoftDeleteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Column(nullable = false, length = 64)
    private String providerUserId;

    private String email;
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public OAuthAccount(OAuthProvider provider, String providerUserId, String email, String nickname, User user) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.nickname = nickname;
        this.user = user;
    }

    public static OAuthAccount link(OAuthProvider provider, String providerUserId, String email, String nickname, User user) {
        return new OAuthAccount(provider, providerUserId, email, nickname, user);
    }

    public void softDelete(Long deleterId) {
        markDeleted(deleterId);
    }

    public void restoreAndRelink (User newUser, String email, String nickname) {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;

        this.user = newUser;
        this.email = email;
        this.nickname = nickname;
    }
}
