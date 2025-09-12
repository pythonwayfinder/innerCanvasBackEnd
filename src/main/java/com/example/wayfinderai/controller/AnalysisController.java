package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.AiCounselingResponseDto;
import com.example.wayfinderai.DTOs.InitialAnalysisResponseDto;
import com.example.wayfinderai.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * 일기 최초 분석을 요청하는 API입니다.
     */
    @PostMapping("/ai")
    public ResponseEntity<InitialAnalysisResponseDto> getAiCounseling(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("diaryId") Long diaryId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("diaryText") String diaryText) {

        InitialAnalysisResponseDto responseDto = analysisService.requestInitialAnalysis(diaryId, file, diaryText, userDetails);

        System.out.println("responseDto: " + responseDto.getCounselingResponse());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 회원 및 비회원의 후속 채팅 메시지를 처리하는 API입니다.
     */
    @PostMapping("/chat")
    public ResponseEntity<AiCounselingResponseDto> handleChatMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> requestBody) {

        String aiResponse = analysisService.processChatMessage(userDetails, requestBody);
        System.out.println("Ai response: " + aiResponse);
        return ResponseEntity.ok(new AiCounselingResponseDto(aiResponse));
    }
}