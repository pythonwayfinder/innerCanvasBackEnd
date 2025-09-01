package com.example.wayfinderai.controller;


import com.example.wayfinderai.DTOs.CalendarResponseDto;
import com.example.wayfinderai.security.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLOutput;
import java.util.List;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService moodService;


    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<CalendarResponseDto>> getMoodData(
            @PathVariable String year,
            @PathVariable String month) {
        System.out.println(year);
        System.out.println(month);
        List<CalendarResponseDto> moodList = moodService.getMoodData(year, month);
        return ResponseEntity.ok(moodList);
    }
}
