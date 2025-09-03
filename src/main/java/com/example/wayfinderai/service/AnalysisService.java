package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.AiCounselingResponseDto;
import com.example.wayfinderai.dto.ChatMessageDto;
import com.example.wayfinderai.dto.ChatResponseDto;
import com.example.wayfinderai.entity.Chat;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.ChatRepository;
import com.example.wayfinderai.repository.DiaryRepository;
import com.example.wayfinderai.security.CustomUserDetails;
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
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final WebClient fastapiWebClient;
    private final PastLogService pastLogService;
    private final DiaryRepository diaryRepository;
    private final ChatRepository chatRepository;

    // =================================================================
    // 역할 1: 최초 분석 요청 처리
    // =================================================================
    public String requestInitialAnalysis(MultipartFile imageFile, String text, String username) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("text", text);
        if (imageFile != null && !imageFile.isEmpty()) {
            bodyBuilder.part("file", imageFile.getResource());
        }

        // 회원의 경우, 지난 7일간의 채팅 기록을 함께 보내 RAG에 활용합니다.
        if (username != null && !username.isEmpty()) {
            String pastLogsJson = pastLogService.getPastLogsAsJson(username);
            if (pastLogsJson != null && !pastLogsJson.isEmpty()) {
                bodyBuilder.part("past_logs_json", pastLogsJson);
            }
        }

        // FastAPI의 최초 분석 엔드포인트로 요청을 보냅니다.
        return fastapiWebClient.post()
                .uri("/analyze/diary/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // =================================================================
    // 역할 2: 후속 채팅 메시지 처리 (RAG)
    // =================================================================
    @Transactional
    public AiCounselingResponseDto processChatMessage(UserDetails userDetails, Map<String, Object> requestBody) {
        String aiMessageText;

        if (userDetails != null) {
            // --- ✨ 회원 로직 ---
            String username = userDetails.getUsername();
            Long diaryId = (Long) requestBody.get("diaryId");
            String message = (String) requestBody.get("message");

            // 1. 사용자의 새 메시지를 DB에 저장합니다.
            saveChatMessage(diaryId, username, "user", message);

            // 2. RAG를 위한 모든 컨텍스트(자료)를 수집합니다.
            Diary diary = diaryRepository.findById(diaryId)
                    .orElseThrow(() -> new EntityNotFoundException("해당 일기를 찾을 수 없습니다: " + diaryId));

            // 2-1. [현재 대화 기록]을 DB에서 조회합니다.
            List<Chat> currentChatHistory = chatRepository.findByDiary_DiaryIdOrderByCreatedAtAsc(diaryId);

            // 2-2. [과거 7일치 모든 대화 기록]을 PastLogService를 통해 조회합니다.
            String past7DaysHistoryJson = pastLogService.getPastLogsAsJson(username);

            // 3. 수집된 모든 자료로 "보강된 프롬프트"를 생성합니다.
            String augmentedPrompt = createAugmentedPromptForMember(diary, currentChatHistory, past7DaysHistoryJson);

            // 4. FastAPI에 프롬프트를 보내 AI의 답변을 요청합니다.
            aiMessageText = callFastApiWithPrompt(augmentedPrompt);

            // 5. AI의 답변도 DB에 저장합니다.
            saveChatMessage(diaryId, member, "ai", aiMessageText);

        } else {
            // --- ✨ 비회원 로직 ---
            // 1. React가 보내준 현재 대화 기록을 FastAPI로 그대로 전달합니다.
            aiMessageText = callFastApiWithGuestPayload(requestBody);
        }

        return new ChatResponseDto(aiMessageText);
    }

    // =================================================================
    // 비공개 헬퍼 메서드 (내부 로직)
    // =================================================================

    private String callFastApiWithPrompt(String prompt) {
        Map<String, String> response = fastapiWebClient.post()
                .uri("/chat/rag/prompt")
                .body(Mono.just(Map.of("prompt", prompt)), Map.class)
                .retrieve().bodyToMono(Map.class).block();
        return response != null ? response.get("message") : "응답을 받지 못했습니다.";
    }

    private String callFastApiWithGuestPayload(Map<String, Object> payload) {
        Map<String, String> response = fastapiWebClient.post()
                .uri("/chat/rag/guest")
                .body(Mono.just(payload), Map.class)
                .retrieve().bodyToMono(Map.class).block();
        return response != null ? response.get("message") : "응답을 받지 못했습니다.";
    }

    private void saveChatMessage(Long diaryId, String userName, String sender, String message) {
        Chat chat = Chat.builder()
                .diary(Diary.builder().diaryId(diaryId).build())
                .member(Member.builder().username(userName).build())
                .sender(sender)
                .message(message)
                .build();
        chatRepository.save(chat);
    }

    // ✨ 회원용 RAG 프롬프트 생성기
    private String createAugmentedPromptForMember(Diary diary, List<Chat> currentChatHistory, String past7DaysHistoryJson) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 전문 심리 상담가입니다. 아래의 모든 정보를 종합하여 사용자의 마지막 질문에 답변해주세요.\n\n");
        prompt.append("--- [현재 일기 정보] ---\n");
        prompt.append("일기 내용: ").append(diary.getDiaryText()).append("\n");
        if (diary.getAiCounselingText() != null) {
            prompt.append("최초 분석: ").append(diary.getAiCounselingText()).append("\n");
        }
        prompt.append("\n--- [현재 대화 기록] ---\n");
        currentChatHistory.forEach(chat -> {
            prompt.append(chat.getSender()).append(": ").append(chat.getMessage()).append("\n");
        });
        prompt.append("\n--- [과거 7일간의 다른 대화 기록 요약] ---\n");
        prompt.append(past7DaysHistoryJson).append("\n\n");
        prompt.append("--- [사용자의 마지막 질문] ---\n");
        prompt.append(currentChatHistory.get(currentChatHistory.size() - 1).getMessage()).append("\n\n");
        prompt.append("답변: ");
        return prompt.toString();
    }
}