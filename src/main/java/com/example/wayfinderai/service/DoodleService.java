package com.example.wayfinderai.service;

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
    public Doodle saveDoodle(Integer userId, String imageUrl, String cnnPrediction) {

        Doodle doodle = new Doodle();
        doodle.setUserId(userId);
        doodle.setImageUrl(imageUrl);
        doodle.setCnnPrediction(cnnPrediction);

        return doodleRepository.save(doodle); // 저장 후 doodle_id 포함된 Entity 반환
    }
}
