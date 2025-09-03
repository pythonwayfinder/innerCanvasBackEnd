package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.DiaryDto;
import com.example.wayfinderai.DTOs.DiaryPostDto;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.wayfinderai.repository.DiaryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public DiaryDto getDiaryByDate(String username, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusSeconds(1);

        Diary diary = diaryRepository.findByMemberUsernameAndCreatedAtBetween(username, start, end)
                .orElseThrow(() -> new RuntimeException("해당 날짜의 일기를 찾을 수 없습니다."));

        return new DiaryDto(diary);
    }

    public DiaryDto createDiary(DiaryPostDto diaryDto) {
        Member member = memberRepository.findByUsername(diaryDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Diary diary = Diary.builder()
                .member(member)
                .diaryText(diaryDto.getDiaryText())
                .moodColor(diaryDto.getMoodColor())
                .createdAt(LocalDateTime.now())
                .build();

        Diary saved = diaryRepository.save(diary);
        return new DiaryDto(saved);
    }
}
