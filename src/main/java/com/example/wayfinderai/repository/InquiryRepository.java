package com.example.wayfinderai.repository;

import com.example.wayfinderai.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByUsernameOrderByIdDesc(String username);
}
