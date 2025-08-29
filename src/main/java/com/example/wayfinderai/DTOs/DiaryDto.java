package com.example.wayfinderai.DTOs;

import com.example.wayfinderai.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryDto {
    private Long diaryId;
    private Long userId;
    private Long doodleId;
    private String diaryText;
    private String moodColor;
    private LocalDateTime createdAt;

    public DiaryDto(Diary diary) {
        this.diaryId = diary.getDiaryId();
        this.userId = diary.getUserId();
        this.doodleId = diary.getDoodleId();
        this.diaryText = diary.getDiaryText();
        this.moodColor = diary.getMoodColor();
        this.createdAt = diary.getCreatedAt();
    }
}
