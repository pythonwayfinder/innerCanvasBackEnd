package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.ChangePasswordRequestDto;
import com.example.wayfinderai.DTOs.LoginRequestDto;
import com.example.wayfinderai.DTOs.UpdateProfileRequestDto;
import com.example.wayfinderai.DTOs.UserDto;
import com.example.wayfinderai.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final MemberService memberService;

    /** 프로필 수정: email, birthDate(→ age 재계산) */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequestDto request
    ) {
        String username = userDetails.getUsername();
        UserDto updated = memberService.updateMyProfile(username, request);
        return ResponseEntity.ok(updated);
    }

    /** 비밀번호 변경 */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordRequestDto request
    ) {
        String username = userDetails.getUsername();
        memberService.changeMyPassword(username, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pass")
    public ResponseEntity<Boolean> changePass(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        return ResponseEntity.ok(memberService.checkPassword(loginRequestDto, response));
    }
}
