package com.example.wayfinderai.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequestDto {
    private String email;        // null이면 변경 안함
    private String birthDate;    // "YYYY-MM-DD", null이면 변경 안함
}
