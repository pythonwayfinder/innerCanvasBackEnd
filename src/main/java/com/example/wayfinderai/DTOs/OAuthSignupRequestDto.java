package com.example.wayfinderai.DTOs;

import lombok.Getter;

@Getter
public class OAuthSignupRequestDto {
    private String username;
    private String birthDate; // "YYYY-MM-DD" 형식의 문자열
}