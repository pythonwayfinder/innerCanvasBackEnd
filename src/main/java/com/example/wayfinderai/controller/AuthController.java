package com.example.wayfinderai.controller;


import com.example.wayfinderai.DTOs.LoginRequestDto;
import com.example.wayfinderai.DTOs.OAuthSignupRequestDto;
import com.example.wayfinderai.DTOs.SignupRequestDto;
import com.example.wayfinderai.DTOs.TokenDto;
import com.example.wayfinderai.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        // Service에서 반환된 TokenDto는 Access Token만 담고 있음
        return ResponseEntity.ok(memberService.login(loginRequestDto, response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(memberService.reissue(request, response));
    }

    @PostMapping("/oauth-signup")
    public ResponseEntity<TokenDto> oauthSignup(@RequestBody OAuthSignupRequestDto requestDto, HttpServletResponse response) {
        return ResponseEntity.ok(memberService.oauthSignup(requestDto, response));
    }
}