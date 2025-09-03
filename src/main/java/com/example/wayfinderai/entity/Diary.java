package com.example.wayfinderai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "diaries")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long diaryId;

    @ManyToOne
    @JoinColumn(name="member_username", referencedColumnName = "username")
    private Member member;

    @Column(name = "diary_text", columnDefinition = "TEXT")
    private String diaryText;

    @Column(name = "mood_color", length = 20)
    private String moodColor;

    @Column(name = "created_at", updatable = false,
            insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
