package com.example.wayfinderai.security.handler; // Security 관련 패키지에 생성


import com.example.wayfinderai.security.service.RefreshTokenService;
import com.example.wayfinderai.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("커스텀 로그아웃 핸들러 실행");
        // 🔄 로직을 아래와 같이 전체 변경합니다.
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            try {
                // 1. 만료 여부와 상관없이 토큰에서 사용자 이름(username)을 가져옵니다.
                String username = jwtUtil.getUsernameFromExpiredToken(refreshToken);

                // 2. Redis에서 해당 username을 키로 가진 Refresh Token을 삭제합니다.
                refreshTokenService.deleteRefreshToken(username);
                log.info("{}님의 Refresh Token을 Redis에서 성공적으로 삭제했습니다.", username);
            } catch (Exception e) {
                // getUsernameFromExpiredToken에서 다른 예외가 발생할 경우를 대비한 로그
                log.error("로그아웃 중 토큰 처리 오류: {}", e.getMessage());
            }
        } else {
            log.warn("쿠키에서 Refresh Token을 찾을 수 없습니다.");
        }
    }

}