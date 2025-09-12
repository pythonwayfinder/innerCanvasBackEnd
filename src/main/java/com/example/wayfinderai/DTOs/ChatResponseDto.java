package com.example.wayfinderai.DTOs;

import com.example.wayfinderai.entity.Chat;
import lombok.Getter;

@Getter
public class ChatResponseDto {
    private String sender;
    private String message;

    public ChatResponseDto(Chat chat) {
        this.sender = chat.getSender();
        this.message = chat.getMessage();
    }
}