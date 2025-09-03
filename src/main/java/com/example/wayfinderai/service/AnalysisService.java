package com.example.wayfinderai.service; // 패키지명은 실제 프로젝트에 맞게 수정해주세요.

 // 과거 기록을 가져오는 서비스
import com.example.wayfinderai.DTOs.ChatResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AnalysisService {

    private final WebClient webClient;
    private final PastLogService pastLogService; // DB에서 과거 기록을 조회하는 서비스 주입

    // WebClient와 함께 PastLogService를 주입받도록 생성자 수정
    public AnalysisService(WebClient fastapiWebClient, PastLogService pastLogService) {
        this.webClient = fastapiWebClient;
        this.pastLogService = pastLogService;
    }

    /**
     * 이미지, 텍스트, 사용자 ID를 받아 FastAPI 서버로 분석을 요청합니다.
     * @param imageFile (선택) 사용자가 업로드한 두들 이미지
     * @param text 사용자가 작성한 일기 텍스트
     * @param username (선택) 현재 로그인한 사용자의 ID
     * @return FastAPI 서버로부터 받은 분석 결과 (JSON 문자열)
     */
    public String requestAnalysisToFastAPI(MultipartFile imageFile, String text, String username) {

        String pastLogsJson = null;

        // --- (핵심 수정) 서비스 내부에서 직접 과거 기록 조회 ---
        // userId가 존재할 경우 (로그인한 회원일 경우) PastLogService를 호출
        if (username != null && !username.isEmpty()) {
            pastLogsJson = pastLogService.getPastLogsAsJson(username);
        }

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // 항상 존재하는 'text' 파트 추가
        bodyBuilder.part("text", text);

        // 이미지 파일이 있을 경우에만 'file' 파트를 추가
        if (imageFile != null && !imageFile.isEmpty()) {
            bodyBuilder.part("file", imageFile.getResource());
        }

//        // 조회된 과거 기록이 있을 경우에만 'past_logs_json' 파트를 추가
//        if (pastLogsJson != null && !pastLogsJson.isEmpty()) {
//            bodyBuilder.part("past_logs_json", pastLogsJson);
//        }

        // WebClient를 사용하여 FastAPI 서버에 POST 요청 전송
        String response = webClient.post()
                .uri("/analyze/diary/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return response;
    }

    public ChatResponseDto processChatMessage(UserDetails userDetails, Map<String, Object> requestBody) {


        return null;
    }
}