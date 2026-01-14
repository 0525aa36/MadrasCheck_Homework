package com.flow.fileextension.domain.extension.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.fileextension.domain.extension.entity.Extension;
import com.flow.fileextension.domain.extension.repository.ExtensionRepository;
import com.flow.fileextension.global.security.SessionUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("확장자 컨트롤러 API 테스트")
class ExtensionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExtensionRepository extensionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        extensionRepository.deleteAll();
        
        // 세션 설정
        session = new MockHttpSession();
        SessionUser sessionUser = new SessionUser("테스트 사용자", "test@test.com", "http://picture.url", 1L);
        session.setAttribute("user", sessionUser);
    }

    @Test
    @DisplayName("GET /api/extensions/fixed - 고정 확장자 목록 조회")
    void getFixedExtensions_Success() throws Exception {
        // given
        Extension fixed1 = Extension.createFixed("exe");
        Extension fixed2 = Extension.createFixed("bat");
        extensionRepository.saveAll(List.of(fixed1, fixed2));

        // when & then
        mockMvc.perform(get("/api/extensions/fixed")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/extensions/custom - 커스텀 확장자 목록 조회")
    void getCustomExtensions_Success() throws Exception {
        // given
        Extension custom1 = Extension.createCustom("pdf");
        Extension custom2 = Extension.createCustom("jpg");
        extensionRepository.saveAll(List.of(custom1, custom2));

        // when & then
        mockMvc.perform(get("/api/extensions/custom")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("PATCH /api/extensions/fixed/{id}/block - 고정 확장자 차단 상태 변경")
    void updateFixedExtensionBlockStatus_Success() throws Exception {
        // given
        Extension fixed = Extension.createFixed("exe");
        Extension saved = extensionRepository.save(fixed);

        // when & then
        mockMvc.perform(patch("/api/extensions/fixed/{id}/block", saved.getId())
                        .param("isBlocked", "true")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 검증
        Extension updated = extensionRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("POST /api/extensions/custom - 커스텀 확장자 추가 성공")
    void addCustomExtension_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/extensions/custom")
                        .param("extension", "pdf")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.extension").value("pdf"));

        // 검증
        List<Extension> extensions = extensionRepository.findAll();
        assertThat(extensions).hasSize(1);
        assertThat(extensions.get(0).getExtension()).isEqualTo("pdf");
    }

    @Test
    @DisplayName("POST /api/extensions/custom - 중복 확장자 추가 실패")
    void addCustomExtension_Duplicate_Fail() throws Exception {
        // given
        Extension existing = Extension.createCustom("pdf");
        extensionRepository.save(existing);

        // when & then
        mockMvc.perform(post("/api/extensions/custom")
                        .param("extension", "pdf")
                        .session(session))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 등록된 확장자입니다."));
    }

    @Test
    @DisplayName("POST /api/extensions/custom - 20자 초과 입력 실패")
    void addCustomExtension_TooLong_Fail() throws Exception {
        // given
        String tooLongExtension = "a".repeat(21);

        // when & then
        mockMvc.perform(post("/api/extensions/custom")
                        .param("extension", tooLongExtension)
                        .session(session))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/extensions/custom - 특수문자 입력 실패")
    void addCustomExtension_InvalidFormat_Fail() throws Exception {
        // when & then
        mockMvc.perform(post("/api/extensions/custom")
                        .param("extension", "pdf!")
                        .session(session))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/extensions/custom - 200개 초과 추가 실패")
    void addCustomExtension_ExceedsLimit_Fail() throws Exception {
        // given
        for (int i = 0; i < 200; i++) {
            Extension custom = Extension.createCustom("ext" + i);
            extensionRepository.save(custom);
        }

        // when & then
        mockMvc.perform(post("/api/extensions/custom")
                        .param("extension", "newext")
                        .session(session))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("커스텀 확장자는 최대 200개까지만 추가할 수 있습니다."));
    }

    @Test
    @DisplayName("DELETE /api/extensions/custom/{id} - 커스텀 확장자 삭제 성공")
    void deleteCustomExtension_Success() throws Exception {
        // given
        Extension custom = Extension.createCustom("pdf");
        Extension saved = extensionRepository.save(custom);

        // when & then
        mockMvc.perform(delete("/api/extensions/custom/{id}", saved.getId())
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 검증
        assertThat(extensionRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/extensions/custom/{id} - 고정 확장자 삭제 실패")
    void deleteCustomExtension_FixedExtension_Fail() throws Exception {
        // given
        Extension fixed = Extension.createFixed("exe");
        Extension saved = extensionRepository.save(fixed);

        // when & then
        mockMvc.perform(delete("/api/extensions/custom/{id}", saved.getId())
                        .session(session))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("고정 확장자는 삭제할 수 없습니다."));
    }

    @Test
    @DisplayName("GET /api/extensions/blocked - 차단된 확장자 목록 조회")
    void getBlockedExtensions_Success() throws Exception {
        // given
        Extension blocked1 = Extension.createCustom("exe");
        blocked1.block();
        Extension blocked2 = Extension.createCustom("bat");
        blocked2.block();
        Extension allowed = Extension.createCustom("pdf");
        extensionRepository.saveAll(List.of(blocked1, blocked2, allowed));

        // when & then
        mockMvc.perform(get("/api/extensions/blocked")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
