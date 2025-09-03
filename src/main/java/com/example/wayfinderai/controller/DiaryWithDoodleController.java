package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.DiaryDto;
import com.example.wayfinderai.DTOs.DiaryPostDto;
import com.example.wayfinderai.entity.Doodle;
import com.example.wayfinderai.service.DiaryService;
import com.example.wayfinderai.service.DoodleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/diary-with-doodle")
@RequiredArgsConstructor
public class DiaryWithDoodleController {

    private final DiaryService diaryService;
    private final DoodleService doodleService;
    //private final AiService aiService; // FastAPI 호출 담당

    @PostMapping
    public String saveDiaryWithDoodle(
            @RequestParam String username,
            @RequestParam String diaryText,
            @RequestParam(required = false) String moodColor,
            @RequestPart(required = false) MultipartFile file
    ) {

        // 3️⃣ FastAPI 호출
//        aiService.sendToAi(savedDiary, savedDoodle);

        // 4️⃣ 응답
//        Map<String, Object> response = new HashMap<>();
//        response.put("diary", savedDiary);
//        response.put("doodle", savedDoodle);

        return "다음 시간에 DiaryWithDoodleController로 전송 성공했습니다: " + LocalDateTime.now();
    }
}