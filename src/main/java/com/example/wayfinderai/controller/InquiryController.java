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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InquiryController {

    private final InquiryService inquiryService;
    private final MemberRepository memberRepository; // ✅ 추가

    @PostMapping("/api/inquiries")
    public String createInquiry(
            @RequestBody InquiryRequestDto inquiryRequest,
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        inquiryService.saveInquiry(member, inquiryRequest.getTitle(), inquiryRequest.getContent());
        return "문의 요청 잘 받았습니다!";
    }

    @GetMapping("/api/inquiries")
    public List<InquiryResponseDto> getMyInquiries(@AuthenticationPrincipal(expression = "username") String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        List<Inquiry> inquiries = inquiryService.getInquiriesByMember(member);

        return inquiries.stream()
                .map(inq -> InquiryResponseDto.builder()
                        .id(inq.getId())
                        .content(inq.getContent())
                        .title(inq.getTitle())
                        .answer(inq.getAnswer())
                        .build()
                )
                .toList();
    }

    @GetMapping("/api/admin/inquiries")// 경로를 /admin으로 구분
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public ResponseEntity<List<InquiryResponseDto>> getAllInquiries() {
        System.out.println("admin 들어옴?");
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    /**
     * 관리자가 특정 문의에 답변을 제출합니다.
     */
    @PostMapping("/api/admin/inquiries/{inquiryId}/answer") // 경로를 /admin으로 구분
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public ResponseEntity<InquiryResponseDto> submitAnswer(
            @PathVariable Long inquiryId,
            @RequestBody Map<String, String> payload) {
        String answerText = payload.get("answer");
        return ResponseEntity.ok(inquiryService.submitAnswer(inquiryId, answerText));
    }
}

