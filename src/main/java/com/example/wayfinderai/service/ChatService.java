package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.DTOs.ChatResponseDto;
import com.example.wayfinderai.entity.Chat;
import com.example.wayfinderai.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;


    public List<ChatResponseDto> getChatByDiaryId(Long diaryId) {
        List<Chat> chats = chatRepository.findByDiaryDiaryId(diaryId);

        return chats.stream()
                .map(ChatResponseDto::new)
                .toList();
    }

    public ChatDto createChat(ChatDto chatDto) {
        Chat chat = new Chat();
        chat.setDiary(chatDto.getDiary());
        chat.setMessage(chatDto.getMessage());
        chat.setSender(chatDto.getSender());
        chat.setCreatedAt(chatDto.getCreatedAt());
        Chat saved = chatRepository.save(chat);
        return new ChatDto(saved);
    }
}
