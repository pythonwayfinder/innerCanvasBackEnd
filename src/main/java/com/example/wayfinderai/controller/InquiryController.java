package com.example.wayfinderai.controller;


import com.example.wayfinderai.DTOs.InquiryRequestDto;
import com.example.wayfinderai.DTOs.InquiryResponseDto;
import com.example.wayfinderai.entity.Inquiry;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.MemberRepository;
import com.example.wayfinderai.service.InquiryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InquiryController {

    private final InquiryService inquiryService;
    private final MemberRepository memberRepository; // ✅ 추가

    @PostMapping
    public String createInquiry(
            @RequestBody InquiryRequestDto inquiryRequest,
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        inquiryService.saveInquiry(member, inquiryRequest.getTitle(), inquiryRequest.getContent());
        return "문의 요청 잘 받았습니다!";
    }

    @GetMapping
    public List<InquiryResponseDto> getMyInquiries(@AuthenticationPrincipal(expression = "username") String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        List<Inquiry> inquiries = inquiryService.getInquiriesByMember(member);

        return inquiries.stream()
                .map(inq -> new InquiryResponseDto(
                        inq.getId(),
                        inq.getTitle(),
                        inq.getContent(),
                        inq.getAnswer()
                ))
                .toList();
    }
}