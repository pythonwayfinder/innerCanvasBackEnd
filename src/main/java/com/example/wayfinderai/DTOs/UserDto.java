package com.example.wayfinderai.DTOs; // dto 패키지에 생성

import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.entity.MemberRoleEnum;
import lombok.Getter;

@Getter
public class UserDto {
    private String username;
    private String email;
    private MemberRoleEnum role;

    public UserDto(Member member) {
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.role = member.getRole();
    }
}