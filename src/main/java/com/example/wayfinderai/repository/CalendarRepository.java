package com.example.wayfinderai.repository;

import com.example.wayfinderai.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    // 특정 년-월에 해당하는 데이터만 조회하는 메서드 작성 (예시)
    List<CalendarEntity> findByDateStartingWith(String yearMonth);
}
