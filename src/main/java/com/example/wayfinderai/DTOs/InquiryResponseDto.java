package com.example.wayfinderai.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InquiryResponseDto {
    private Long id;
    private String title;
    private String content;
    private String answer;
}
