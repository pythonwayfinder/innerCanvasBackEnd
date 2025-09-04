package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.AiCounselingResponseDto;
import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.DTOs.ChatResponseDto;
import com.example.wayfinderai.service.AnalysisService;
import com.example.wayfinderai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/ai")
    public ResponseEntity<AiCounselingResponseDto> getAiCounseling(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("diaryId") Long diaryId,
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
        String counselingResult = analysisService.requestInitialAnalysis(diaryId ,file, diaryText, username);
        System.out.println(counselingResult);
        // 아래는 로직이 구현되었다고 가정한 임시 응답 데이터입니다.
//        String counselingResult = "AI가 사용자의 일기와 그림을 분석한 결과입니다. "
//                + "오늘은 전반적으로 긍정적인 감정이 느껴지네요. "
//                + "그림에서는 자유로운 선의 사용이 인상적입니다. "
//                + "이러한 감정을 계속 이어나가시면 좋겠습니다.";

        // --- 6. Spring이 응답을 DTO에 담아 React로 보냄 ---
        AiCounselingResponseDto responseDto = new AiCounselingResponseDto(counselingResult);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * [창구 2: 후속 채팅] - ✨ 여기가 핵심입니다.
     * @param userDetails (선택) 로그인한 사용자의 정보. 비회원은 null.
     * @param requestBody React가 보낸 JSON 본문 전체를 Map으로 받습니다.
     */
    @PostMapping("/chat")
    public ResponseEntity<AiCounselingResponseDto> handleChatMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> requestBody) { // DTO 대신 Map 사용
        System.out.println("Received Chat Message: " + requestBody);
        // AnalysisService에 Map을 그대로 전달하여 처리를 위임합니다.
//        String aiResponse = analysisService.processChatMessage(userDetails, requestBody);
        String aiResponse = "답변 1";

        AiCounselingResponseDto responseDto = new AiCounselingResponseDto(aiResponse);

        return ResponseEntity.ok(responseDto);
    }

}
