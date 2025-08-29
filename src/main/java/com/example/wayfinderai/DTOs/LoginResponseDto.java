package com.example.wayfinderai.DTOs; // dto 패키지에 생성

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String accessToken;
    private UserDto user;

    public LoginResponseDto(String accessToken, UserDto user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}