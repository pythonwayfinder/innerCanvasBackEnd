package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.DiaryDto;
import com.example.wayfinderai.DTOs.DiaryPostDto;
import com.example.wayfinderai.service.DiaryService;
import com.example.wayfinderai.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping
    public ResponseEntity<DiaryDto> getDiary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String date
    ) {
        String username = userDetails.getUsername();
        LocalDate localDate = LocalDate.parse(date); // "2025-08-28"
        return ResponseEntity.ok(diaryService.getDiaryByDate(username, localDate));
    }

    @PostMapping
    public ResponseEntity<DiaryDto> createDiary(@AuthenticationPrincipal UserDetails userDetails, @RequestBody DiaryPostDto diaryDto) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(diaryService.createDiary(username, diaryDto));
    }
}
