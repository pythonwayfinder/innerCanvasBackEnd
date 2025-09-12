package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.ChatDto;
import com.example.wayfinderai.DTOs.ChatResponseDto;
import com.example.wayfinderai.DTOs.DiaryDto;
import com.example.wayfinderai.DTOs.DiaryPostDto;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Doodle;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.DoodleRepository;
import com.example.wayfinderai.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.wayfinderai.repository.DiaryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final DoodleRepository doodleRepository;
    private final ChatService chatService;

    public DiaryDto getDiaryByDate(String username, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusSeconds(1);

        // --- 수정된 부분 2: .orElseThrow() 대신 Optional을 직접 처리합니다. ---
        Optional<Diary> diaryOptional = diaryRepository.findByMemberUsernameAndCreatedAtBetween(username, start, end);
        if (diaryOptional.isPresent()) {
            Diary diary = diaryOptional.get();
            Optional<Doodle> doodleOptional = doodleRepository.findByDiary_DiaryId(diary.getDiaryId());
            List<ChatResponseDto> chatDtos = chatService.getChatByDiaryId(diary.getDiaryId());
            return DiaryDto.builder()
                    .diaryId(diary.getDiaryId())
                    .diaryText(diary.getDiaryText())
                    .createdAt(diary.getCreatedAt())
                    .member(diary.getMember())
                    .moodColor(diary.getMoodColor())
                    .doodleUrl(doodleOptional.map(Doodle::getImageUrl).orElse(null))
                    .chatDtos(chatDtos)
                    .build();
        }

        // diaryOptional에 값이 있으면 DiaryDto로 변환(map)하고, 없으면 null을 반환(orElse)합니다.
        return null;
    }

    public DiaryDto createDiary(String username, DiaryPostDto diaryDto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Diary diary = Diary.builder()
                .member(member)
                .diaryText(diaryDto.getDiaryText())
                .moodColor(diaryDto.getMoodColor())
                .createdAt(LocalDateTime.now())
                .build();

        Diary saved = diaryRepository.save(diary);
        System.out.println(saved.getDiaryId());
        return new DiaryDto(saved);
    }
}
