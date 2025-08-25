package com.example.wayfinderai.service;


import com.example.wayfinderai.DTOs.LoginRequestDto;
import com.example.wayfinderai.DTOs.OAuthSignupRequestDto;
import com.example.wayfinderai.DTOs.SignupRequestDto;
import com.example.wayfinderai.DTOs.TokenDto;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.entity.MemberRoleEnum;
import com.example.wayfinderai.repository.MemberRepository;
import com.example.wayfinderai.security.service.RefreshTokenService;
import com.example.wayfinderai.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpiration;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());
        String email = requestDto.getEmail();
        MemberRoleEnum role = requestDto.isAdmin() ? MemberRoleEnum.ADMIN : MemberRoleEnum.USER;

        if (memberRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 username 입니다.");
        }
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member member = Member.builder()
                .username(username)
                .password(password)
                .email(email)
                .role(role)
                .provider("local") // 일반 회원가입은 "local"로 저장
                .build();
        memberRepository.save(member);
    }

    @Transactional
    public TokenDto login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = memberRepository.findByUsername(requestDto.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getUsername(), member.getRole());

        // 🔄 변경 후: substring(7)을 사용하여 "Bearer "를 제거하고 저장합니다.
        refreshTokenService.saveRefreshToken(member.getUsername(), refreshToken.substring(7));

        // Refresh Token을 HttpOnly 쿠키에 담아 응답
        Cookie refreshTokenCookie = new Cookie(JwtUtil.REFRESH_TOKEN_HEADER, refreshToken.substring(7)); // "Bearer " 제거
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS를 사용할 때 true로 설정
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        response.addCookie(refreshTokenCookie);

        // Access Token만 DTO에 담아 반환
        return new TokenDto(accessToken, null);
    }

    @Transactional
    public TokenDto reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        // 🔄 변경 전: if (refreshToken == null || !jwtUtil.validateToken("Bearer " + refreshToken))
        // 🔄 변경 후: "Bearer " + 부분을 삭제합니다.
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 🔄 변경 전: String username = jwtUtil.getUserInfoFromToken("Bearer " + refreshToken).getSubject();
        // 🔄 변경 후: "Bearer " + 부분을 삭제합니다.
        String username = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        String storedRefreshToken = refreshTokenService.findRefreshToken(username);

        // 🔄 변경 전: if (storedRefreshToken == null || !refreshToken.equals(storedRefreshToken.substring(7)))
        // 🔄 변경 후: 이제 Redis에 저장된 값도 순수한 토큰이므로, substring(7)을 제거합니다.
        if (storedRefreshToken == null || !refreshToken.equals(storedRefreshToken)) {
            System.out.println("storedRefreshToken: " + storedRefreshToken);
            System.out.println("refreshToken: " + refreshToken);
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")
        );

        // 새로운 Access Token 생성
        String newAccessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole());

        // Access Token만 DTO에 담아 반환
        return new TokenDto(newAccessToken, null);
    }

    @Transactional
    public TokenDto oauthSignup(OAuthSignupRequestDto requestDto, HttpServletResponse response) {
        // 🔄 수정: JWT 임시 토큰 검증 및 파싱
        String tempToken = requestDto.getTempToken();
        if (!jwtUtil.validateToken(tempToken)) {
            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
        }

        Claims claims = jwtUtil.getUserInfoFromToken(tempToken);
        String email = claims.get("email", String.class);
        String provider = claims.get("provider", String.class);
        String username = requestDto.getUsername();

        // 2. username 또는 email 중복 확인
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 username 입니다.");
        }
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 3. 사용자 정보로 최종 회원가입
        Member newMember = Member.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("OAUTH_USER_PASSWORD")) // 소셜 로그인 유저는 실제 비밀번호가 없으므로 임의의 값 저장
                .role(MemberRoleEnum.USER)
                .provider(provider)
                .build();
        memberRepository.save(newMember);

        // 4. JWT 발급 및 응답
        String accessToken = jwtUtil.createAccessToken(newMember.getUsername(), newMember.getRole());
        String refreshToken = jwtUtil.createRefreshToken(newMember.getUsername(), newMember.getRole());

        refreshTokenService.saveRefreshToken(newMember.getUsername(), refreshToken);
        // (addRefreshTokenToCookie 메서드를 외부에 만들거나 MemberService 내에 구현하여 사용)
        // addRefreshTokenToCookie(response, refreshToken);

        return new TokenDto(accessToken, null);
    }
}