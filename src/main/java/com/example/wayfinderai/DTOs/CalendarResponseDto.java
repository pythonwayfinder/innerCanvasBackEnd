package com.example.wayfinderai.DTOs;

import lombok.Getter;

@Getter
public class CalendarResponseDto {
    private String date;
    private String month;

    public CalendarResponseDto(String date, String month) {
        this.date = date;
        this.month = month;
    }
}
