package com.example.wayfinderai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "doodles")
@Getter @Setter
public class Doodle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doodle_id")
    private Integer doodleId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "cnn_prediction", length = 50)
    private String cnnPrediction;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
