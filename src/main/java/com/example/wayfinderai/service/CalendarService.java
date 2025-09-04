package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.CalendarResponseDto;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final DiaryRepository diaryRepository;

    public List<CalendarResponseDto> getMoodData(String year, String month) {
        // 한 자리 월에 0 붙이기 (예: 9 -> 09)
        String formattedMonth = month.length() == 1 ? "0" + month : month;

        // 월의 시작일과 마지막일 계산
        LocalDate startDate = LocalDate.parse(year + "-" + formattedMonth + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // LocalDateTime으로 변환 (시작은 자정, 끝은 23:59:59)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 해당 기간 사이의 Diary 데이터 조회
        List<Diary> diaries = diaryRepository.findByCreatedAtBetween(startDateTime, endDateTime);

        // DTO로 변환
        List<CalendarResponseDto> moodList = new ArrayList<>();
        for (Diary diary : diaries) {
            moodList.add(new CalendarResponseDto(
                    diary.getCreatedAt().toLocalDate().toString(),
                    diary.getMoodColor()
            ));
        }

        return moodList;
    }
}