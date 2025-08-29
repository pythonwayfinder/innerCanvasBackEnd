package com.example.wayfinderai.repository;

import com.example.wayfinderai.entity.Doodle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoodleRepository extends JpaRepository<Doodle, Long> {
}

