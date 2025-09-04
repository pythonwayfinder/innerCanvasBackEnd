package com.example.wayfinderai.DTOs;

import lombok.Getter;

@Getter
public class CalendarResponseDto {
    private String date;
    private String moodColor;

    public CalendarResponseDto(String date, String moodColor) {
        this.date = date;
        this.moodColor = moodColor;
    }
}