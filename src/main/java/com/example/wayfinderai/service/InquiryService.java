package com.example.wayfinderai.service;

import com.example.wayfinderai.entity.Inquiry;
import com.example.wayfinderai.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    // 문의 저장
    public Inquiry saveInquiry(String username, String title, String content) {
        Inquiry inquiry = new Inquiry(username, title, content);
        return inquiryRepository.save(inquiry);
    }

    // 유저별 문의 리스트 조회
    public List<Inquiry> getInquiriesByUsername(String username) {
        return inquiryRepository.findByUsernameOrderByIdDesc(username);
    }
}
