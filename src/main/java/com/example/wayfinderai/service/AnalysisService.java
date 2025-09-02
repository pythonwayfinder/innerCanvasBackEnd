package com.example.wayfinderai.service;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AnalysisService {

    private final WebClient webClient;

    // 2단계에서 설정한 WebClient를 주입받음
    public AnalysisService(WebClient fastapiWebClient) {
        this.webClient = fastapiWebClient;
    }

    public String analyzeDiary(MultipartFile imageFile) {

        // 1. MultipartBodyBuilder를 사용하여 'multipart/form-data' 요청 본문을 생성
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // 2. 'file'이라는 이름(key)으로 이미지 파일을 추가.
        // 이 'file'은 FastAPI 엔드포인트의 `file: UploadFile = File(...)` 부분의 변수 이름과 일치해야 함.
        bodyBuilder.part("file", imageFile.getResource());

        // 3. WebClient를 사용하여 FastAPI 서버에 POST 요청 전송
        String response = webClient.post()           // POST 요청
                .uri("/analyze_diary/")          // 목적지 경로
                .contentType(MediaType.MULTIPART_FORM_DATA) // 데이터 타입은 multipart
                .body(BodyInserters.fromMultipartData(bodyBuilder.build())) // 1, 2번에서 만든 요청 본문 삽입
                .retrieve()                       // 응답을 받기 시작
                .bodyToMono(String.class)         // 응답 본문을 String 형태로 변환
                .block();                         // 비동기 작업이 끝날 때까지 기다림

        return response; // FastAPI로부터 받은 JSON 응답을 그대로 반환
    }
}