package com.example.wayfinderai.DTOs;

import lombok.Getter;

@Getter
public class OAuthSignupRequestDto {
    private String username;
    private String tempToken; // 임시 토큰
}