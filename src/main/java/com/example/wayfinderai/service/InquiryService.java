package com.example.wayfinderai.service;

import com.example.wayfinderai.DTOs.InquiryResponseDto;
import com.example.wayfinderai.entity.Inquiry;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.InquiryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    public Inquiry saveInquiry(Member member, String title, String content) {
        Inquiry inquiry = Inquiry.builder()
                .title(title)
                .content(content)
                .member(member)
                .answer(null)
                .status(Inquiry.InquiryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        return inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getInquiriesByMember(Member member) {
        return inquiryRepository.findByMemberOrderByIdDesc(member);
    }

    // 모든 문의 내역을 조회하는 메서드
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getAllInquiries() {
        return inquiryRepository.findAllByOrderByStatusAscCreatedAtDesc()
                .stream()
                .map(inq -> InquiryResponseDto.builder()
                        .id(inq.getId())
                        .username(inq.getMember().getUsername()) // Member 객체에서 username 추출
                        .title(inq.getTitle())
                        .content(inq.getContent())
                        .answer(inq.getAnswer())
                        .status(inq.getStatus())
                        .createdAt(inq.getCreatedAt())
                        .build() // 마지막에 build()를 호출하여 객체 생성 완료
                )
                .toList();
    }

    // 특정 문의에 답변을 등록하는 메서드
    @Transactional
    public InquiryResponseDto submitAnswer(Long inquiryId, String answerText) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("해당 문의를 찾을 수 없습니다: " + inquiryId));

        inquiry.setAnswer(answerText);
        inquiry.setStatus(Inquiry.InquiryStatus.ANSWERED);

        // JpaRepository.save()는 변경 감지(dirty checking)에 의해 자동으로 호출되므로 생략 가능
        // inquiryRepository.save(inquiry);

        return InquiryResponseDto.builder()
                .id(inquiry.getId())
                .username(inquiry.getMember().getUsername())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .answer(answerText)
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
