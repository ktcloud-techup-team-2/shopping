package com.kt.security;

import com.kt.common.CustomException;
import com.kt.common.ErrorCode;
import com.kt.security.dto.TokenRequestDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    private final JwtProperties jwtProperties;

    public TokenRequestDto generateToken(Authentication authentication, Long userId) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();

        Date accessTokenExpiresIn = jwtProperties.getAccessTokenExpiration();
        Date refreshTokenExpiresIn = jwtProperties.getRefreshTokenExpiration();

        String accessToken = Jwts.builder()
                .claims().subject(userId.toString())
                .issuedAt(now)
                .add(AUTHORITIES_KEY, authorities)
                .expiration(accessTokenExpiresIn)
                .and()
                .signWith(jwtProperties.getSecretKey())
                .compact();

        String refreshToken = Jwts.builder()
                .claims()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(refreshTokenExpiresIn)
                .and()
                .signWith(jwtProperties.getSecretKey())
                .compact();

        return new TokenRequestDto(
                userId,
                BEARER_TYPE,
                accessToken,
                refreshToken
        );
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Object authClaims = claims.get(AUTHORITIES_KEY);
        if(authClaims == null) {
            throw new CustomException(ErrorCode.MISSING_AUTHORITY);
        }

        List<GrantedAuthority> authorities = Arrays.stream(authClaims.toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        String userId = claims.getSubject();
        User principal = new User(userId, "",authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtProperties.getSecretKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (SignatureException e) {
            log.error("JWT_SIGNATURE_FAIL: {}", e.getMessage());
            throw new CustomException(ErrorCode.JWT_SIGNATURE_FAIL);
        } catch (MalformedJwtException e) {
            log.error("JWT_Decode FAIL: {}", e.getMessage());
            throw new CustomException(ErrorCode.JWT_DECODE_FAIL);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.error("UNAUTHORIZED_CLIENT : {}", e.getMessage());
            throw new CustomException(ErrorCode.UNAUTHORIZED_CLIENT);
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtProperties.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }catch (Exception e) {
            // 커스텀 예외로 변경 예정
            throw e;
        }
    }
}
