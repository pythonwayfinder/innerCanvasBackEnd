package com.example.wayfinderai.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
