package com.example.wayfinderai.DTOs;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LoginRequestDto {
    private String username;
    private String password;
    private LocalDate birthDate;
}

