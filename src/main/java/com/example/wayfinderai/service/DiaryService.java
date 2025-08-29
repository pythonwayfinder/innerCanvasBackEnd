package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.DiaryDto;
import com.example.wayfinderai.entity.Diary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.wayfinderai.repository.DiaryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;

    public DiaryDto getDiaryByDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusSeconds(1);

        Diary diary = diaryRepository.findByUserIdAndCreatedAtBetween(userId, start, end)
                .orElseThrow(() -> new RuntimeException("해당 날짜의 일기를 찾을 수 없습니다."));

        return new DiaryDto(diary);
    }

    public DiaryDto createDiary(DiaryDto diaryDto) {
        Diary diary = new Diary();
        diary.setUserId(diaryDto.getUserId());
        diary.setDiaryText(diaryDto.getDiaryText());
        diary.setMoodColor(diaryDto.getMoodColor());
        diary.setDoodleId(diaryDto.getDoodleId());

        Diary saved = diaryRepository.save(diary);
        return new DiaryDto(saved);
    }
}
