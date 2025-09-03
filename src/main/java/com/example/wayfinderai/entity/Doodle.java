package com.example.wayfinderai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "doodles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doodle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doodle_id")
    private Integer doodleId;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="diary_id", referencedColumnName = "diary_id")
    private Diary diary;

    @Column(name = "cnn_prediction", length = 50)
    private String cnnPrediction;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
