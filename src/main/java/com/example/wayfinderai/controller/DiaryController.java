package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.DiaryDto;
import com.example.wayfinderai.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping
    public DiaryDto getDiary(
            @RequestParam String username,
            @RequestParam String date
    ) {
        LocalDate localDate = LocalDate.parse(date); // "2025-08-28"
        return diaryService.getDiaryByDate(username, localDate);
    }

    @PostMapping
    public DiaryDto createDiary(@RequestBody DiaryDto diaryDto) {
        return diaryService.createDiary(diaryDto);
    }
}
