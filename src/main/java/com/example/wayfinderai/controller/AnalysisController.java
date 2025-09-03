package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.AiCounselingResponseDto;
import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.service.AnalysisService;
import com.example.wayfinderai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/ai")
    public ResponseEntity<AiCounselingResponseDto> getAiCounseling(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("diaryText") String diaryText) {
        // ---1. username 가져오기
        String username = null;
        if (userDetails != null) {
            username = userDetails.getUsername();
        }

        // --- 2. Spring이 React로부터 이미지와 텍스트를 받음 ---
        System.out.println("Received Diary Text: " + diaryText);
        if (file != null && !file.isEmpty()) {
            System.out.println("Received File Name: " + file.getOriginalFilename());
            System.out.println("Received File Size: " + file.getSize());
        }

         //--- 2-1, 2-2, 3, 4, 5. FastAPI 호출 및 LLM 응답 처리 로직 (Service 계층에서 처리) ---
//         String counselingResult = analysisService.requestAnalysisToFastAPI(file, diaryText, username);

        // 아래는 로직이 구현되었다고 가정한 임시 응답 데이터입니다.
        String counselingResult = "AI가 사용자의 일기와 그림을 분석한 결과입니다. "
                + "오늘은 전반적으로 긍정적인 감정이 느껴지네요. "
                + "그림에서는 자유로운 선의 사용이 인상적입니다. "
                + "이러한 감정을 계속 이어나가시면 좋겠습니다.";


        // --- 6. Spring이 응답을 DTO에 담아 React로 보냄 ---
        AiCounselingResponseDto responseDto = new AiCounselingResponseDto(counselingResult);
        return ResponseEntity.ok(responseDto);
    }
}
