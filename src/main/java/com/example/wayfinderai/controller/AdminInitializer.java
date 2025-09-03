package com.example.wayfinderai.controller; // 또는 config 패키지

import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.entity.MemberRoleEnum;
import com.example.wayfinderai.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String adminUsername = "admin";
        String adminPassword = "password"; // 실제 운영 시에는 환경 변수 사용을 권장합니다.
        String adminEmail = "admin@example.com"; // 관리자용 이메일

        // 관리자 계정이 이미 존재하는지 확인
        if (!memberRepository.findByUsername(adminUsername).isPresent()) {

            // --- 여기가 핵심 수정 부분 ---
            // Member 엔티티의 빌더에 새로운 필수/권장 필드를 추가합니다.
            Member admin = Member.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .email(adminEmail) // email 필드 추가
                    .role(MemberRoleEnum.ADMIN)
                    .provider("local") // 자체 로그인이므로 provider를 "local"로 명시
                    .age(0) // 필수 필드인 age에 기본값(0) 설정
                    .birthDate(null) // 선택 필드인 birthDate는 null로 설정 가능
                    .build();

            memberRepository.save(admin);
            System.out.println("✅ 초기 관리자 계정이 생성되었습니다. (username: " + adminUsername + ")");
        }
    }
}