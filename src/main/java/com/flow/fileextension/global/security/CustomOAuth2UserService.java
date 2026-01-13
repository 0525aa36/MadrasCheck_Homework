package com.flow.fileextension.global.security;

import com.flow.fileextension.domain.user.entity.User;
import com.flow.fileextension.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User user = findOrCreateUser(email, name, picture);

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User findOrCreateUser(String email, String name, String picture) {
        return userRepository.findByEmail(email)
                .map(this::updateLastLogin)
                .orElseGet(() -> createNewUser(email, name, picture));
    }

    private User updateLastLogin(User user) {
        user.updateLastLogin();
        return userRepository.save(user);
    }

    private User createNewUser(String email, String name, String picture) {
        return userRepository.save(User.builder()
                .email(email)
                .name(name)
                .profileImage(picture)
                .build());
    }
}
