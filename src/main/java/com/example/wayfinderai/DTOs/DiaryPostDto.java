package com.example.wayfinderai.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiaryPostDto {
    private String userName;
    private String diaryText;
    private String moodColor;

    public DiaryPostDto(String userName, String diaryText, String moodColor) {
        this.userName = userName;
        this.diaryText = diaryText;
        this.moodColor = moodColor;
    }
}
