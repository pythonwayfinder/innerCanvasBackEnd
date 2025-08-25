package com.example.wayfinderai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 직접 입력하는 로그인 ID 또는 닉네임
    @Column(nullable = false, unique = true)
    private String username;

    // 소셜 로그인은 비밀번호가 없을 수 있으므로 nullable = true로 변경
    @Column(nullable = true)
    private String password;

    // 이메일 필드 추가 (OAuth2 식별용 + 비즈니스 로직용)
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MemberRoleEnum role;

    // 어느 소셜 로그인을 통해 가입했는지 확인 (예: "google", "local")
    @Column
    private String provider;

    @Builder
    public Member(String username, String password, String email, MemberRoleEnum role, String provider) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.provider = provider;
    }
}