package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.DoodleDto;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Doodle;
import com.example.wayfinderai.repository.DiaryRepository;
import com.example.wayfinderai.repository.DoodleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DoodleService {
    private final DoodleRepository doodleRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public Doodle saveDoodle(Long diaryId, String imageUrl, String cnnPrediction) {

        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 다이어리입니다."));

        Doodle doodle = Doodle.builder()
                .diary(diary)
                .imageUrl(imageUrl)
                .cnnPrediction("") // 필요 시 초기값
                .createdAt(LocalDateTime.now())
                .build();

        return doodleRepository.save(doodle); // 저장 후 doodle_id 포함된 Entity 반환
    }

    public DoodleDto getDoodle(Integer doodleId) {
        Doodle doodle = doodleRepository.findByDoodleId(doodleId).orElseThrow(
                () -> new RuntimeException("해당 번호의 두들을 찾을 수 없습니다.")
        );

        return new DoodleDto(doodle);
    }
}
