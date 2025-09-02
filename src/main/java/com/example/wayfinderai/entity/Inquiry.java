package com.example.wayfinderai.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inquiries")
@Data
@NoArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="member_username", referencedColumnName = "username")
    private Member member;  // 문의자 아이디 (ex. 로그인한 유저 이메일)

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer; // 답변 내용 (없을 수도 있음)

    // 생성자 편의 생성
    public Inquiry(String username, String title, String content) {
        this.username = username;
        this.title = title;
        this.content = content;
    }
}