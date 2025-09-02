package com.example.wayfinderai.DTOs;

import lombok.Getter;

// AI 상담 결과를 담아 React로 보낼 DTO
@Getter
public class AiCounselingResponseDto {
    private String counselingText;

    // 생성자, Getter, Setter
    public AiCounselingResponseDto(String counselingText) {
        this.counselingText = counselingText;
    }

    public void setCounselingText(String counselingText) {
        this.counselingText = counselingText;
    }
}