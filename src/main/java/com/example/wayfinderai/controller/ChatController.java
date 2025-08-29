package com.example.wayfinderai.controller;

import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public List<ChatDto> getChats(@RequestParam Integer diaryId) {
        return chatService.getChatByDiaryId(diaryId);
    }

    @PostMapping
    public ChatDto saveChat(@RequestBody ChatDto chatDto) {
        return chatService.createChat(chatDto);
    }
}
