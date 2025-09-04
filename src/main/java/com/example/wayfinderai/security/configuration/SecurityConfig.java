package com.example.wayfinderai.security.configuration;


import com.example.wayfinderai.security.JwtAuthenticationFilter;
import com.example.wayfinderai.security.UserDetailsServiceImpl;
import com.example.wayfinderai.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationSuccessHandler oAuth2LoginSuccessHandler;
    private final LogoutHandler customLogoutHandler;



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF, Form Login, HTTP Basic 비활성화
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 세션 관리 STATELESS로 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 인가 규칙 설정
        http.authorizeHttpRequests(auth -> auth
                // ✨ 1. 더 구체적인 규칙을 먼저 작성합니다.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/auth/me").authenticated() // '/api/auth/me'는 반드시 인증이 필요함
                // ✨ 2. 그 외 더 넓은 범위의 규칙을 나중에 작성합니다.
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/analysis/**").permitAll()
//                .requestMatchers("/api/doodles/**", "/api/diary/**", "/api/chat/**", "/api/diary-with-doodle/**").permitAll()
                .requestMatchers("/", "/index.html", "/script.js", "/main/**", "/api/img/**").permitAll()
                .anyRequest().authenticated()
        );

        http.exceptionHandling(exception -> exception
                // 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때의 동작을 정의합니다.
                // 이 규칙은 permitAll() 경로라도, 인증 정보가 잘못되었을 때를 대비해 항상 적용될 수 있습니다.
                .authenticationEntryPoint((request, response, authException) -> {
                    // 이 요청이 API 요청인지 확인합니다 (React의 axios는 'X-Requested-With' 헤더를 보냅니다).
                    boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
                    if (isAjax) {
                        // API 요청일 경우, 절대 리디렉션하지 않고 401 Unauthorized 에러를 응답합니다.
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    } else {
                        // API 요청이 아닌 일반적인 브라우저 페이지 이동일 경우에만 로그인 페이지로 리디렉션합니다.
                        response.sendRedirect("/login");
                    }
                })
        );


        // OAuth2 로그인 설정 추가
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 시 핸들러 사용
        );

        // ✨ 1. Spring Security의 내장 로그아웃 설정을 추가합니다.
        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout") // 로그아웃을 처리할 URL을 지정합니다.
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> {
                    // 로그아웃 성공 시 별도의 리디렉션 없이 성공 상태 코드(200 OK)만 반환합니다.
                    response.setStatus(HttpServletResponse.SC_OK);
                })
                .deleteCookies(JwtUtil.REFRESH_TOKEN_HEADER) // 로그아웃 시 RefreshToken 쿠키를 삭제합니다.
        );

        // JWT 필터 추가
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✨ 1. CORS 설정을 위한 Bean을 추가합니다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 주소를 허용합니다.
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:5173"));
        // 허용할 HTTP 메서드를 설정합니다.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 허용할 헤더를 설정합니다.
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // 자격 증명(쿠키 등)을 허용합니다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위 설정 Dto 적용합니다.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}