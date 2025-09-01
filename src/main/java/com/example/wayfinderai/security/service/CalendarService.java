package com.example.wayfinderai.security.service;

import com.example.wayfinderai.DTOs.CalendarResponseDto;
import com.example.wayfinderai.entity.CalendarEntity;
import com.example.wayfinderai.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor  // lombok으로 생성자 자동 생성
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public List<CalendarResponseDto> getMoodData(String year, String month) {
        // month가 1자리 숫자면 앞에 0 붙이기 (예: 8 -> 08)
        String formattedMonth = month.length() == 1 ? "0" + month : month;
        String yearMonth = year + "-" + formattedMonth; // ex) "2025-08"

        // DB에서 해당 년-월로 시작하는 날짜 리스트 조회
        List<CalendarEntity> entities = calendarRepository.findByDateStartingWith(yearMonth);

        List<CalendarResponseDto> moodList = new ArrayList<>();
        for (CalendarEntity entity : entities) {
            moodList.add(new CalendarResponseDto(entity.getDate(), entity.getMonth()));
        }
        return moodList;
    }
}