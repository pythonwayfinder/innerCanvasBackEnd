package com.example.wayfinderai.repository;

import com.example.wayfinderai.entity.Doodle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoodleRepository extends JpaRepository<Doodle, Long> {
    Optional<Doodle> findByDoodleId(Integer doodleId);

    Optional<Doodle> findByDiary_DiaryId(Long diaryDiaryId);
}

