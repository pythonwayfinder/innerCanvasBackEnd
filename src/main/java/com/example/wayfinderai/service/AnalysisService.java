package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.GuestChatRequestDto;
import com.example.wayfinderai.DTOs.InitialAnalysisResponseDto;
import com.example.wayfinderai.DTOs.MemberChatRequestDto;
import com.example.wayfinderai.entity.Chat;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.ChatRepository;
import com.example.wayfinderai.repository.DiaryRepository;
import com.example.wayfinderai.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final WebClient fastapiWebClient;
    private final DiaryRepository diaryRepository;
    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;

    // FastAPI에 일기 최초 분석을 요청하고, 결과를 받아 DB에 첫 AI 메시지를 저장합니다.
    @Transactional
    public InitialAnalysisResponseDto requestInitialAnalysis(Long diaryId, MultipartFile imageFile, String text, UserDetails userDetails) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("diary_id", diaryId);
        bodyBuilder.part("text", text);
        if (imageFile != null && !imageFile.isEmpty()) {
            bodyBuilder.part("file", imageFile.getResource());
        }

        String username = (userDetails != null) ? userDetails.getUsername() : null;
        if (username != null) {
            bodyBuilder.part("username", username);
        }

        // FastAPI의 /analyze/diary/ 엔드포인트로 요청
        InitialAnalysisResponseDto response = fastapiWebClient.post()
                .uri("/analyze/diary/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(InitialAnalysisResponseDto.class)
                .block();

        if (response == null) {
            throw new RuntimeException("FastAPI로부터 분석 결과를 받지 못했습니다.");
        }

        // 회원인 경우, AI의 첫 답변과 감정 분석 결과를 DB에 저장
        if (username != null) {
            saveChatMessage(diaryId, username, "ai", response.getCounselingResponse(), response.getMainEmotion());
        }

        return response;
    }

    // 사용자의 후속 채팅 메시지를 처리합니다. (회원/비회원 분기 처리)
    @Transactional
    public String processChatMessage(UserDetails userDetails, Map<String, Object> requestBody) {
        if (userDetails != null) {
            return processMemberChat(userDetails.getUsername(), requestBody);
        } else {
            return processGuestChat(requestBody);
        }
    }

    // 회원의 채팅 메시지를 FastAPI로 보내고, 유저와 AI의 대화를 DB에 저장합니다.
    private String processMemberChat(String username, Map<String, Object> requestBody) {
        Long diaryId = Long.parseLong(requestBody.get("diaryId").toString());
        String userMessage = (String) requestBody.get("message");

        saveChatMessage(diaryId, username, "user", userMessage, null);

        MemberChatRequestDto payload = new MemberChatRequestDto(diaryId, username, userMessage);
        String aiMessage = callFastApi("/analyze/chat", payload, MemberChatRequestDto.class);

        saveChatMessage(diaryId, username, "ai", aiMessage, null);
        return aiMessage;
    }

    // 비회원의 채팅 메시지를 FastAPI로 보내 답변을 받습니다. (DB 저장 없음)
    private String processGuestChat(Map<String, Object> requestBody) {
        String tempUsername = (String) requestBody.get("temp_username");
        String userMessage = (String) requestBody.get("message");

        GuestChatRequestDto payload = new GuestChatRequestDto(tempUsername, userMessage);
        return callFastApi("/analyze/chat/guest", payload, GuestChatRequestDto.class);
    }

    // FastAPI 채팅 API를 호출하는 공통 메서드입니다.
    @SuppressWarnings("unchecked")
    private <T> String callFastApi(String uri, T payload, Class<T> payloadType) {
        Map<String, String> response = fastapiWebClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("message") == null) {
            return "응답을 받지 못했습니다.";
        }
        return response.get("message");
    }

    // 채팅 메시지를 DB에 저장하고, 필요한 경우 다이어리의 감정 색상을 업데이트합니다.
    private void saveChatMessage(Long diaryId, String userName, String sender, String message, String emotionType) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new EntityNotFoundException("해당 Diary를 찾을 수 없습니다: " + diaryId));

        if (emotionType != null && !emotionType.isEmpty()) {
            diary.setMoodColor(emotionType);
        }

        Member member = memberRepository.findByUsername(userName)
                .orElseThrow(() -> new EntityNotFoundException("해당 Member를 찾을 수 없습니다: " + userName));

        Chat chat = Chat.builder()
                .diary(diary)
                .member(member)
                .sender(sender)
                .message(message)
                .build();
        chatRepository.save(chat);
    }
}