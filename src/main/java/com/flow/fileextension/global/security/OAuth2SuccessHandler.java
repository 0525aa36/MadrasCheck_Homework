package com.flow.fileextension.global.security;

import com.flow.fileextension.domain.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 시 세션에 사용자 정보를 저장하는 핸들러
 */
@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // 세션에 사용자 정보 저장
        HttpSession session = request.getSession();
        SessionUser sessionUser = new SessionUser(user);
        session.setAttribute("user", sessionUser);

        log.info("OAuth2 로그인 성공 - 사용자 정보를 세션에 저장: id={}, name={}",
                sessionUser.getId(), sessionUser.getName());

        // 프론트엔드로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
