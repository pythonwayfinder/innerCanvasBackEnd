package com.example.wayfinderai.utils;


import com.example.wayfinderai.entity.MemberRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String REFRESH_TOKEN_HEADER = "RefreshToken";

    private static final long TEMP_TOKEN_EXPIRATION_MS = 300000; // 5분

    @Value("${jwt.secret.key}")
    private String secretKey;
    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpiration;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // Access Token 생성
    public String createAccessToken(String username, MemberRoleEnum role) {
        return createToken(username, role, accessTokenExpiration);
    }

    // Refresh Token 생성
    public String createRefreshToken(String username, MemberRoleEnum role) {
        return createToken(username, role, refreshTokenExpiration);
    }

    private String createToken(String username, MemberRoleEnum role, long expiration) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(expireDate) // 만료 시간
                        .setIssuedAt(now) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // ✨ 추가: 임시 토큰 생성 메서드
    public String createTempToken(String email, String provider) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + TEMP_TOKEN_EXPIRATION_MS);

        return Jwts.builder()
                .claim("email", email)
                .claim("provider", provider)
                .setExpiration(expireDate)
                .setIssuedAt(now)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    // HTTP Request Header에서 토큰 가져오기
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Request의 쿠키에서 Refresh Token 가져오기
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(REFRESH_TOKEN_HEADER)) {

                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // ✨ 추가: 만료된 토큰에서도 사용자 이름을 가져오는 메서드
    public String getUsernameFromExpiredToken(String token) {
        try {
            // 일반적인 방법으로 사용자 이름 추출 시도
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우, 예외에서 Claims를 직접 얻어 사용자 이름 추출
            return e.getClaims().getSubject();
        }
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}