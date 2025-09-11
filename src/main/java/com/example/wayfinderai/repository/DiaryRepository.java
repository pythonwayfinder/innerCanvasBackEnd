package com.example.wayfinderai.repository;

import com.example.wayfinderai.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByDiaryId(Long diaryId);

    Optional<Diary> findByMemberUsernameAndCreatedAtBetween(
            String username,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Diary> findAllByMemberUsername(String username);

    // 캘린더 로직
    List<Diary> findByMember_UsernameAndCreatedAtBetween(String username, LocalDateTime startDateTime, LocalDateTime endDateTime);
}


