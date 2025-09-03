package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.DoodleDto;
import com.example.wayfinderai.entity.Diary;
import com.example.wayfinderai.entity.Doodle;
import com.example.wayfinderai.repository.DoodleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DoodleService {
    private final DoodleRepository doodleRepository;

    public DoodleService(DoodleRepository doodleRepository) {
        this.doodleRepository = doodleRepository;
    }

    @Transactional
    public Doodle saveDoodle(Diary diary, String imageUrl, String cnnPrediction) {

        Doodle doodle = new Doodle();
        doodle.setDiary(diary);
        doodle.setImageUrl(imageUrl);
        doodle.setCnnPrediction(cnnPrediction);

        return doodleRepository.save(doodle); // 저장 후 doodle_id 포함된 Entity 반환
    }

    public DoodleDto getDoodle(Integer doodleId) {
        Doodle doodle = doodleRepository.findByDoodleId(doodleId).orElseThrow(
                () -> new RuntimeException("해당 번호의 두들을 찾을 수 없습니다.")
        );

        return new DoodleDto(doodle);
    }
}
