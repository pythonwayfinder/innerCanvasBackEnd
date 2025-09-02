package com.example.wayfinderai.DTOs;

import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.entity.MemberRoleEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserDto {
    private String username;
    private String email;
    private MemberRoleEnum role;
    private Integer age;
    private LocalDate birthDate; // "YYYY-MM-DD" or null

    // ✅ 서비스 코드의 new UserDto(member) 호출과 맞는 생성자
    public UserDto(Member member) {
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.role = member.getRole();
        this.age = member.getAge();
        this.birthDate = member.getBirthDate();
    }
}
