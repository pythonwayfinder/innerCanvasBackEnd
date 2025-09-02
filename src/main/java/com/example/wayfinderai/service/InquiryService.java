package com.example.wayfinderai.service;

import com.example.wayfinderai.entity.Inquiry;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    public Inquiry saveInquiry(Member member, String title, String content) {
        Inquiry inquiry = new Inquiry(member, title, content);
        return inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getInquiriesByMember(Member member) {
        return inquiryRepository.findByMemberOrderByIdDesc(member);
    }
}
