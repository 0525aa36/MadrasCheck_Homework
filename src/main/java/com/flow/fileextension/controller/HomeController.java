package com.flow.fileextension.controller;

import com.flow.fileextension.global.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        if (customOAuth2User != null) {
            model.addAttribute("userName", customOAuth2User.getName());
        }
        return "index";
    }
}
