package com.example.wayfinderai.service;

import com.example.wayfinderai.entity.Chat;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.ChatRepository;
import com.example.wayfinderai.repository.DiaryRepository;
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

import java.util.HashMap;
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
    public String requestInitialAnalysis(Long diaryId, MultipartFile imageFile, String text, String username) {
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
        Map<String, String> response = fastapiWebClient.post()
                .uri("/analyze/diary/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(Map.class) // String 대신 Map으로 받도록 변경
                .block();

        String aiConselingText = response != null ? response.get("counseling_response") : "분석 결과를 받지 못했습니다.";
        saveChatMessage(diaryId, username, "ai", aiConselingText);
        // Map에서 "message" 키를 가진 값을 추출하여 반환합니다.
        return response != null ? response.get("counseling_response") : "분석 결과를 받지 못했습니다.";
    }

    // =================================================================
    // 역할 2: 후속 채팅 메시지 처리 (RAG)
    // =================================================================
    @Transactional
    public String processChatMessage(UserDetails userDetails, Map<String, Object> requestBody) {
        String aiMessageText;
        Map<String, Object> payloadToFastApi = new HashMap<>(requestBody); // React가 보낸 데이터를 기반으로 payload 생성

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

            payloadToFastApi.put("currentChatHistory", currentChatHistory); // 현재 대화 기록
            payloadToFastApi.put("past7DaysHistory", past7DaysHistoryJson); // 과거 7일치 모든 대화

            return "이건 ai 답변";

//            // 4. 완성된 payload를 FastAPI로 전송합니다.
//            aiMessageText = callFastApiForChat(payloadToFastApi);
//
//            // 5. AI의 답변도 DB에 저장합니다.
//            saveChatMessage(diaryId, username, "ai", aiMessageText);

        } else {
            // --- ✨ 비회원 로직 ---
            // 1. React가 보내준 현재 대화 기록을 FastAPI로 그대로 전달합니다.
            return "이건 ai 답변";

//            aiMessageText = callFastApiWithGuestPayload(requestBody);
        }



//        return aiMessageText;
    }

    // =================================================================
    // 비공개 헬퍼 메서드 (내부 로직)
    // =================================================================

    private String callFastApiForChat(Map<String, Object> payload) {
        // 이제 모든 채팅 관련 FastAPI 응답은 { "message": "..." } JSON 형식이라고 가정합니다.
        Map<String, String> response = fastapiWebClient.post()
                .uri("/analyze/chat") // FastAPI의 통합 채팅 엔드포인트
                .body(Mono.just(payload), Map.class)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return response != null ? response.get("message") : "응답을 받지 못했습니다.";
    }

    private String callFastApiWithGuestPayload(Map<String, Object> payload) {
        Map<String, String> response = fastapiWebClient.post()
                .uri("/analyze/chat/guest")
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
}