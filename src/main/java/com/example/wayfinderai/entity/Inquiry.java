package com.example.wayfinderai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Data
@DynamicUpdate
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_username", referencedColumnName = "username", nullable = false) // FK
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.PENDING; // 기본 상태를 'PENDING'으로 설정

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum InquiryStatus {
        PENDING, // 답변 대기
        ANSWERED // 답변 완료
    }
}
