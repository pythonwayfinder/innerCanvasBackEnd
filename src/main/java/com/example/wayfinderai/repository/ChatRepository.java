package com.example.wayfinderai.repository;

import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {

    List<Chat> findByDiaryId(Integer diaryId);
}
