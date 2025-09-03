package com.example.wayfinderai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "diaries")
@Getter @Setter
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long diaryId;

    @ManyToOne
    @JoinColumn(name="member_username", referencedColumnName = "username")
    private Member member;

    @Column(name = "doodle_id")
    private Long doodleId; // 추후 Doodle 엔티티 연관관계로 변경 가능

    @Column(name = "diary_text", columnDefinition = "TEXT")
    private String diaryText;

    @Column(name = "mood_color", length = 20)
    private String moodColor;

    @Column(name = "created_at", updatable = false,
            insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
