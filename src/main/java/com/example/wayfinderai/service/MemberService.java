package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.*;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.entity.MemberRoleEnum;
import com.example.wayfinderai.repository.MemberRepository;
import com.example.wayfinderai.security.service.RefreshTokenService;
import com.example.wayfinderai.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

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

        LocalDate birthDate = LocalDate.parse(requestDto.getBirthDate());
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        Member member = Member.builder()
                .username(username)
                .password(password)
                .email(email)
                .role(role)
                .provider("local") // 일반 회원가입은 "local"로 저장
                .age(age) // ✨ 계산된 나이 저장
                .build();

        memberRepository.save(member);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = memberRepository.findByUsername(requestDto.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );
        System.out.println("12312312312312312321323" + member);
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getUsername(), member.getRole());

        refreshTokenService.saveRefreshToken(member.getUsername(), refreshToken.substring(7));

        // Refresh Token을 HttpOnly 쿠키에 담아 응답
        Cookie refreshTokenCookie = new Cookie(JwtUtil.REFRESH_TOKEN_HEADER, refreshToken.substring(7)); // "Bearer " 제거
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS를 사용할 때 true로 설정
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        response.addCookie(refreshTokenCookie);

        // ✨ Member Entity를 UserDto로 변환
        UserDto userDto = new UserDto(member);

        // ✨ AccessToken과 UserDto를 함께 담아 반환
        return new LoginResponseDto(accessToken, userDto);
    }

    @Transactional
    public TokenDto reissue(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("refreshToken 재발급");
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
    public LoginResponseDto oauthSignup(OAuthSignupRequestDto requestDto, String email, String provider, HttpServletResponse response) {
        String username = requestDto.getUsername();

        // 2. username 또는 email 중복 확인
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 username 입니다.");
        }
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        LocalDate birthDate = LocalDate.parse(requestDto.getBirthDate());
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        // 3. 사용자 정보로 최종 회원가입
        Member newMember = Member.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("OAUTH_USER_PASSWORD")) // 소셜 로그인 유저는 실제 비밀번호가 없으므로 임의의 값 저장
                .role(MemberRoleEnum.USER)
                .age(age)
                .provider(provider)
                .build();
        memberRepository.save(newMember);

        // 4. JWT 발급 및 응답
        String accessToken = jwtUtil.createAccessToken(newMember.getUsername(), newMember.getRole());
        String refreshToken = jwtUtil.createRefreshToken(newMember.getUsername(), newMember.getRole());

        refreshTokenService.saveRefreshToken(newMember.getUsername(), refreshToken);
        // (addRefreshTokenToCookie 메서드를 외부에 만들거나 MemberService 내에 구현하여 사용)
        // addRefreshTokenToCookie(response, refreshToken);

        // ✨ Member Entity를 UserDto로 변환
        UserDto userDto = new UserDto(newMember);

        // ✨ AccessToken과 UserDto를 함께 담아 반환
        return new LoginResponseDto(accessToken, userDto);
    }

    @Transactional
    public UserDto getUserInfo(String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 없습니다.")
        );
        return new UserDto(member);
    }

    @Transactional
    public boolean checkUsernameAvailability(String username) {
        return !memberRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public UserDto updateMyProfile(String username, UpdateProfileRequestDto dto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이메일 변경 (null/blank면 무시)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            member.setEmail(dto.getEmail().trim());
        }

        // 생년월일이 전달되면 → age 재계산 후 저장 (DB에는 age만 저장)
        if (dto.getBirthDate() != null && !dto.getBirthDate().isBlank()) {
            LocalDate birth = LocalDate.parse(dto.getBirthDate()); // "YYYY-MM-DD"
            int age = Period.between(birth, LocalDate.now()).getYears();
            member.setAge(age);
        }

        // 변경사항 저장
        memberRepository.save(member);

        // 최신 상태 반환
        return new UserDto(member);
    }

    @Transactional
    public void changeMyPassword(String username, ChangePasswordRequestDto dto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 검증
        if (member.getPassword() == null || !passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 확인 일치 검증
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인이 일치하지 않습니다.");
        }

        // 기존과 동일 비밀번호 방지(선택)
        if (passwordEncoder.matches(dto.getNewPassword(), member.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일합니다.");
        }

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        memberRepository.save(member);
    }
}