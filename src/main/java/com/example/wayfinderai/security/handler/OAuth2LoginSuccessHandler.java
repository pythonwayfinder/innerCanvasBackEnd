package com.example.wayfinderai.security.handler; // 적절한 패키지에 생성


import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.MemberRepository;
import com.example.wayfinderai.security.service.RefreshTokenService;
import com.example.wayfinderai.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공!");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String provider = "google"; // 공급자 정보 (예: google)

        // DB에서 이메일로 사용자 조회
        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        if (memberOptional.isPresent()) {
            // 이미 가입된 사용자인 경우, 로그인 처리
            Member member = memberOptional.get();
            log.info("기존 사용자입니다. 로그인을 진행합니다.");

            // JWT 발급
            String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole());
            String refreshToken = jwtUtil.createRefreshToken(member.getUsername(), member.getRole());

            // 🔄 변경 후: substring(7)을 사용하여 "Bearer "를 제거하고 저장합니다.
            refreshTokenService.saveRefreshToken(member.getUsername(), refreshToken.substring(7));
            addRefreshTokenToCookie(response, refreshToken);

            // Access Token을 쿼리 파라미터로 담아 리디렉션
            String redirectUrl = "http://localhost:8080?accessToken=" + accessToken.substring(7);
            response.sendRedirect(redirectUrl);

        } else {
            // 신규 사용자인 경우, 추가 정보 입력을 위해 임시 토큰 발급 후 리디렉션
            log.info("신규 사용자입니다. 추가 정보 입력 페이지로 리디렉션합니다.");

            // 🔄 수정: 단순 문자열 대신 JWT 임시 토큰 생성
            String tempToken = jwtUtil.createTempToken(email, provider);

            String redirectUrl = "http://localhost:8080/?tempToken=" + tempToken;
            response.sendRedirect(redirectUrl);
        }
    }


    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(JwtUtil.REFRESH_TOKEN_HEADER, refreshToken.substring(7));
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        response.addCookie(refreshTokenCookie);
    }
}