package com.example.wayfinderai.controller;


import com.example.wayfinderai.DTOs.*;
import com.example.wayfinderai.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto signupRequestDto) {
        memberService.signup(signupRequestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    // 🔄 반환 타입을 ResponseEntity<TokenDto>에서 ResponseEntity<LoginResponseDto>로 변경
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        return ResponseEntity.ok(memberService.login(loginRequestDto, response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(memberService.reissue(request, response));
    }

    @PostMapping("/oauth-signup")
    public ResponseEntity<LoginResponseDto> oauthSignup(@RequestBody OAuthSignupRequestDto requestDto, HttpSession session, HttpServletResponse response) {

        String email = (String) session.getAttribute("oauth_email");
        String provider = (String) session.getAttribute("oauth_provider");

        if (email == null || provider == null) {
            throw new IllegalArgumentException("세션 정보가 유효하지 않습니다.");
        }

        // 세션에서 꺼낸 정보와 DTO를 서비스로 전달
        LoginResponseDto loginResponse = memberService.oauthSignup(requestDto, email, provider, response);

        // 사용이 끝난 세션 정보는 삭제
        session.removeAttribute("oauth_email");
        session.removeAttribute("oauth_provider");

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("/me 들어왔음");
        String username = userDetails.getUsername();
        UserDto userInfo = memberService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(memberService.checkUsernameAvailability(username));
    }
}