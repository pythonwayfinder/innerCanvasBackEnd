package com.example.wayfinderai.DTOs;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String username;
    private String password;
    private String email; // 이메일 필드 추가
    private boolean admin;
}
