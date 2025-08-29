package com.example.wayfinderai.DTOs;

import com.example.wayfinderai.entity.Doodle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoodleDto {
    private Integer doodleID;
    private Integer userID;
    private String imageUrl;
    private String cnnPrediction;
    private LocalDateTime createdAt;

    public DoodleDto(Doodle doodle) {
        this.doodleID = doodle.getDoodleId();
        this.userID = doodle.getUserId();
        this.imageUrl = doodle.getImageUrl();
        this.cnnPrediction = doodle.getCnnPrediction();
        this.createdAt = doodle.getCreatedAt();
    }

}
