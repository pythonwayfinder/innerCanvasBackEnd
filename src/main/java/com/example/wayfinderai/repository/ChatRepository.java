package com.example.wayfinderai.repository;

import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.entity.Chat;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    List<Chat> findByDiaryDiaryId(Long diaryId);

    // Member 엔티티와 createdAt 날짜를 기준으로
    // 특정 날짜 이후의 모든 채팅 기록을 시간순으로 정렬하여 찾아옵니다.
    List<Chat> findByMemberAndCreatedAtAfterOrderByCreatedAtAsc(Member member, LocalDateTime startDate);

    List<Chat> findByDiary_DiaryIdOrderByCreatedAtAsc(Integer diaryId);
}
