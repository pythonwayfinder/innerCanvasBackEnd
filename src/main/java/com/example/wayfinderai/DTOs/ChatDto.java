package com.example.wayfinderai.DTOs;

import com.example.wayfinderai.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private Integer chatId;
    private Integer diaryId;
    private String sender;
    private String message;
    private LocalDateTime createdAt;

    public ChatDto(Chat chat) {
        this.chatId = chat.getChatId();
        this.diaryId = chat.getDiaryId();
        this.sender = chat.getSender();
        this.message = chat.getMessage();
        this.createdAt = chat.getCreatedAt();
    }
}
