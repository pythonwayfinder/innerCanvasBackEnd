package com.example.wayfinderai.repository;

import com.example.wayfinderai.entity.Inquiry;
import com.example.wayfinderai.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByMemberOrderByIdDesc(Member member);

    List<Inquiry> findAllByOrderByStatusAscCreatedAtDesc();
}
