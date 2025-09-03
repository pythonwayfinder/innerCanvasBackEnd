package com.example.wayfinderai.service;

import com.example.wayfinderai.entity.Chat;
import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.repository.ChatRepository;
import com.example.wayfinderai.repository.MemberRepository;
import com.example.wayfinderai.DTOs.ChatLogDto; // 데이터 전송에 사용할 DTO
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 데이터를 조회만 하므로 readOnly=true로 성능 최적화
@Slf4j // 로그 출력을 위한 어노테이션
public class PastLogService {

    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;
    private final ObjectMapper objectMapper; // Java 객체를 JSON으로 변환하기 위한 Jackson 라이브러리

    /**
     * 사용자 이름(username)을 기반으로 지난 7일간의 채팅 기록을 조회하여 JSON 문자열로 반환합니다.
     * @param username 조회할 사용자의 이름
     * @return 채팅 기록을 담은 JSON 문자열. 오류 발생 시 빈 JSON 배열 "[]" 반환
     */
    public String getPastLogsAsJson(String username) {
        // 1. username으로 Member 엔티티를 찾습니다. 없으면 예외를 발생시킵니다.
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));

        // 2. 조회 시작 날짜를 현재로부터 7일 전으로 설정합니다.
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 3. ChatRepository를 통해 해당 사용자의 7일치 채팅 기록을 DB에서 조회합니다.
        List<Chat> chatLogs = chatRepository.findByMemberAndCreatedAtAfterOrderByCreatedAtAsc(member, sevenDaysAgo);

        // 4. 조회된 Chat 엔티티 목록을 프론트엔드로 보내기 좋은 ChatLogDto 목록으로 변환합니다.
        List<ChatLogDto> chatLogDtos = chatLogs.stream()
                .map(ChatLogDto::new)
                .collect(Collectors.toList());

        // 5. DTO 목록을 JSON 문자열로 직렬화(serialize)합니다.
        try {
            return objectMapper.writeValueAsString(chatLogDtos);
        } catch (JsonProcessingException e) {
            // JSON 변환 중 오류가 발생하면 로그를 남기고, 빈 배열 형태의 JSON을 반환합니다.
            log.error("채팅 기록을 JSON으로 변환하는 데 실패했습니다.", e);
            return "[]";
        }
    }
}