package com.flow.fileextension.global.config;

import com.flow.fileextension.global.security.CustomOAuth2UserService;
import com.flow.fileextension.global.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 토큰 핸들러 설정 (SPA 환경 지원)
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null); // Spring Security 6.x 이상 권장

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                "/api/auth/**",  // OAuth2 로그인/로그아웃은 CSRF 검증 제외
                                "/oauth2/**",
                                "/login/**"
                        )
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(authorize -> authorize
                        // 조회 API (비로그인 허용)
                        .requestMatchers("/api/extensions/fixed", "/api/extensions/custom", "/api/extensions/blocked").permitAll()
                        .requestMatchers("/api/file/check").permitAll()
                        .requestMatchers("/api/auth/**").permitAll() // 인증 관련 API
                        
                        // 수정 API (로그인 필수)
                        .requestMatchers("/api/extensions/**").authenticated()
                        
                        .anyRequest().permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl(frontendUrl)
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
}
