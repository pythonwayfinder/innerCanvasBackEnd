package com.example.wayfinderai.repository;


import com.example.wayfinderai.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    // email로 사용자를 찾는 메서드 추가
    Optional<Member> findByEmail(String email);
}