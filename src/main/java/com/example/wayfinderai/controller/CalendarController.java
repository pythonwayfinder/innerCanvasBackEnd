package com.example.wayfinderai.controller;


import com.example.wayfinderai.DTOs.CalendarResponseDto;
import com.example.wayfinderai.DTOs.MoodRequestDto;
import com.example.wayfinderai.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService moodService;

    @PostMapping("/{year}/{month}")
    public ResponseEntity<List<CalendarResponseDto>> postMoodData(
            @PathVariable String year,
            @PathVariable String month,
            @RequestBody MoodRequestDto requestDto) { // 2. @RequestBody로 username 받기

        System.out.println("Year: " + year);
        System.out.println("Month: " + month);
        System.out.println("Username: " + requestDto.getUsername()); // 3. username 확인

        // 4. 서비스 호출 시 username 전달
        List<CalendarResponseDto> moodList = moodService.getMoodData(year, month, requestDto.getUsername());
        return ResponseEntity.ok(moodList);
    }
}
