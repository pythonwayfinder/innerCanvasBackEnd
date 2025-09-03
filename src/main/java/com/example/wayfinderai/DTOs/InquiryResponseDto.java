package com.example.wayfinderai.DTOs;

import com.example.wayfinderai.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class InquiryResponseDto {
    private final Long id;
    private final String username; // 문의를 작성한 사용자 이름
    private final String title;
    private final String content;
    private final String answer;
    private final Inquiry.InquiryStatus status; // PENDING 또는 ANSWERED
    private final LocalDateTime createdAt;

}
