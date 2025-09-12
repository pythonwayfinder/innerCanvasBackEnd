package com.example.wayfinderai.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberChatRequestDto {

    private Long diaryId;
    private String username;
    private String message;
}