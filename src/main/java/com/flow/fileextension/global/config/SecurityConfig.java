package com.flow.fileextension.global.config;

import com.flow.fileextension.global.security.CustomOAuth2UserService;
import com.flow.fileextension.global.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint; // Import HttpStatusEntryPoint

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(authorize -> authorize
                        // 메인 페이지 및 정적 리소스 (비로그인 허용)
                        .requestMatchers("/", "/error", "/actuator/health", "/css/**", "/images/**", "/js/**").permitAll()
                        
                        // 조회 API (비로그인 허용)
                        .requestMatchers("/api/extensions/fixed", "/api/extensions/custom", "/api/extensions/blocked").permitAll()
                        .requestMatchers("/api/file/check").permitAll()
                        
                        // 수정 API (로그인 필수)
                        .requestMatchers("/api/extensions/**").authenticated()
                        
                        // 나머지는 모두 허용
                        .anyRequest().permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/")  // 로그인 페이지를 메인 페이지로 설정
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
}
