package com.example.wayfinderai.security.handler; // 적절한 패키지에 생성


import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.MemberRepository;
import com.example.wayfinderai.security.service.RefreshTokenService;
import com.example.wayfinderai.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

    // ✨ 리액트 프론트엔드 주소를 주입받습니다.
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String provider = "google";

        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        Member member;

        if (memberOptional.isPresent()) {
            // 기존 사용자는 바로 로그인 처리
            member = memberOptional.get();
            log.info("기존 사용자입니다. 로그인을 진행합니다.");

            String refreshToken = jwtUtil.createRefreshToken(member.getUsername(), member.getRole());
            refreshTokenService.saveRefreshToken(member.getUsername(), refreshToken.substring(7));
            addRefreshTokenToCookie(response, refreshToken);

            // ✨ AccessToken 없이, 프론트엔드의 콜백 페이지로 리디렉션
            response.sendRedirect(frontendUrl + "/oauth/callback");

        } else {
            // 신규 사용자는 세션에 정보 저장 후 추가 정보 입력 페이지로
            log.info("신규 사용자입니다. 추가 정보 입력 페이지로 리디렉션합니다.");
            HttpSession session = request.getSession();
            session.setAttribute("oauth_email", email);
            session.setAttribute("oauth_provider", provider);
            session.setMaxInactiveInterval(300);

            // ✨ tempToken 없이, 프론트엔드의 OAuth 전용 회원가입 페이지로 리디렉션
            response.sendRedirect(frontendUrl + "/signup/oauth");
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