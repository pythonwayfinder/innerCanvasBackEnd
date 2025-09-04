package com.example.wayfinderai.DTOs;

import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Member;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryDto {
    private Long diaryId;
    private Member member;
    private String doodleUrl;
    private String diaryText;
    private String moodColor;
    private LocalDateTime createdAt;
    private List<ChatDto> chatDtos;

    public DiaryDto(Diary diary) {
        this.diaryId = diary.getDiaryId();
        this.member = diary.getMember();
        this.diaryText = diary.getDiaryText();
        this.moodColor = diary.getMoodColor();
        this.createdAt = diary.getCreatedAt();
    }
}
