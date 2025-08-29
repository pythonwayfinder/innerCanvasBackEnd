package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.entity.Chat;
import com.example.wayfinderai.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public List<ChatDto> getChatByDiaryId(Integer diaryId) {
        List<Chat> chats = chatRepository.findByDiaryId(diaryId);

        return chats.stream()
                .map(ChatDto::new)
                .toList();
    }

    public ChatDto createChat(ChatDto chatDto) {
        Chat chat = new Chat();
        chat.setDiaryId(chatDto.getDiaryId());
        chat.setMessage(chatDto.getMessage());
        chat.setSender(chatDto.getSender());
        chat.setCreatedAt(chatDto.getCreatedAt());
        Chat saved = chatRepository.save(chat);
        return new ChatDto(saved);
    }
}
