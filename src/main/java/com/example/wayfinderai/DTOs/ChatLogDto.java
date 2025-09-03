package com.example.wayfinderai.DTOs;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ChatLogDto {
    private final Integer diaryId;
    private final String sender;
    private final String message;
    private final LocalDateTime createdAt;

    // Chat 엔티티를 ChatLogDto로 변환하는 생성자
    public ChatLogDto(Chat chat) {
        this.diaryId = chat.getDiaryId();
        this.sender = chat.getSender();
        this.message = chat.getMessage();
        this.createdAt = chat.getCreatedAt();
    }
}