package com.example.wayfinderai.controller; // config 패키지나 별도 패키지에 추가


import com.example.wayfinderai.entity.Member;
import com.example.wayfinderai.entity.MemberRoleEnum;
import com.example.wayfinderai.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String adminUsername = "admin";
        String adminPassword = "password"; // 실제 운영 시에는 더 복잡한 비밀번호 사용 또는 환경 변수 사용

        // 관리자 계정이 이미 존재하는지 확인
        if (!memberRepository.findByUsername(adminUsername).isPresent()) {
            // 없다면 생성
            Member admin = Member.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(MemberRoleEnum.ADMIN)
                    .build();

            memberRepository.save(admin);
            System.out.println("✅ 초기 관리자 계정이 생성되었습니다.");
        }
    }
}