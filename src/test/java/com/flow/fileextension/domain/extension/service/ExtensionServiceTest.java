package com.flow.fileextension.domain.extension.service;

import com.flow.fileextension.domain.extension.entity.Extension;
import com.flow.fileextension.domain.extension.repository.ExtensionRepository;
import com.flow.fileextension.global.constants.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("확장자 서비스 통합 테스트")
class ExtensionServiceTest {

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private ExtensionRepository extensionRepository;

    @BeforeEach
    void setUp() {
        extensionRepository.deleteAll();
    }

    @Test
    @DisplayName("고정 확장자 목록 조회")
    void getFixedExtensions_Success() {
        // given
        Extension fixed1 = Extension.createFixed("exe");
        Extension fixed2 = Extension.createFixed("bat");
        extensionRepository.saveAll(List.of(fixed1, fixed2));

        // when
        List<Extension> result = extensionService.getFixedExtensions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Extension::isFixed);
    }

    @Test
    @DisplayName("커스텀 확장자 목록 조회")
    void getCustomExtensions_Success() {
        // given
        Extension custom1 = Extension.createCustom("pdf");
        Extension custom2 = Extension.createCustom("jpg");
        extensionRepository.saveAll(List.of(custom1, custom2));

        // when
        List<Extension> result = extensionService.getCustomExtensions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(ext -> !ext.isFixed());
    }

    @Test
    @DisplayName("고정 확장자 차단 상태 변경")
    void updateFixedExtensionBlockStatus_Success() {
        // given
        Extension fixedExt = Extension.createFixed("exe");
        Extension saved = extensionRepository.save(fixedExt);
        Long userId = 1L;

        // when
        extensionService.updateFixedExtensionBlockStatus(saved.getId(), true, userId);

        // then
        Extension updated = extensionRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.isBlocked()).isTrue();
        assertThat(updated.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("커스텀 확장자 추가 - 성공")
    void addCustomExtension_Success() {
        // given
        String extension = "pdf";
        Long userId = 1L;

        // when
        Extension result = extensionService.addCustomExtension(extension, userId);

        // then
        assertThat(result.getExtension()).isEqualTo("pdf");
        assertThat(result.isFixed()).isFalse();
        assertThat(result.isBlocked()).isTrue(); // 기본값 차단
        assertThat(result.getCreatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("중복 확장자 추가 시도 - 예외 발생")
    void addCustomExtension_Duplicate_ThrowsException() {
        // given
        Extension existing = Extension.createCustom("pdf");
        extensionRepository.save(existing);
        Long userId = 1L;

        // when & then
        assertThatThrownBy(() -> extensionService.addCustomExtension("pdf", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.DUPLICATE_EXTENSION);
    }

    @Test
    @DisplayName("대소문자 구분 없이 중복 체크")
    void addCustomExtension_CaseInsensitiveDuplicate_ThrowsException() {
        // given
        Extension existing = Extension.createCustom("pdf");
        extensionRepository.save(existing);
        Long userId = 1L;

        // when & then
        assertThatThrownBy(() -> extensionService.addCustomExtension("PDF", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.DUPLICATE_EXTENSION);
    }

    @Test
    @DisplayName("커스텀 확장자 200개 제한 검증")
    void addCustomExtension_ExceedsLimit_ThrowsException() {
        // given
        for (int i = 0; i < 200; i++) {
            Extension custom = Extension.createCustom("ext" + i);
            extensionRepository.save(custom);
        }
        Long userId = 1L;

        // when & then
        assertThatThrownBy(() -> extensionService.addCustomExtension("newext", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.MAX_CUSTOM_EXTENSIONS);
    }

    @Test
    @DisplayName("커스텀 확장자 삭제 - 성공")
    void deleteCustomExtension_Success() {
        // given
        Extension custom = Extension.createCustom("pdf");
        Extension saved = extensionRepository.save(custom);

        // when
        extensionService.deleteCustomExtension(saved.getId());

        // then
        assertThat(extensionRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 확장자 삭제 시도 - 예외 발생")
    void deleteCustomExtension_NotFound_ThrowsException() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> extensionService.deleteCustomExtension(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.EXTENSION_NOT_FOUND);
    }

    @Test
    @DisplayName("고정 확장자는 삭제 불가")
    void deleteCustomExtension_FixedExtension_ThrowsException() {
        // given
        Extension fixed = Extension.createFixed("exe");
        Extension saved = extensionRepository.save(fixed);

        // when & then
        assertThatThrownBy(() -> extensionService.deleteCustomExtension(saved.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessages.CANNOT_DELETE_FIXED);
    }

    @Test
    @DisplayName("차단된 확장자 목록 조회")
    void getBlockedExtensions_Success() {
        // given
        Extension blocked1 = Extension.createCustom("exe");
        blocked1.block();
        Extension blocked2 = Extension.createCustom("bat");
        blocked2.block();
        Extension allowed = Extension.createCustom("pdf");
        extensionRepository.saveAll(List.of(blocked1, blocked2, allowed));

        // when
        List<String> result = extensionService.getBlockedExtensions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("exe", "bat");
    }

    @Test
    @DisplayName("확장자 정규화 테스트 - 점 제거 및 소문자 변환")
    void addCustomExtension_Normalization_Success() {
        // given
        Long userId = 1L;

        // when
        Extension result1 = extensionService.addCustomExtension(".PDF", userId);
        extensionRepository.delete(result1); // 중복 방지를 위해 삭제
        Extension result2 = extensionService.addCustomExtension("Jpg", userId);

        // then
        assertThat(result1.getExtension()).isEqualTo("pdf");
        assertThat(result2.getExtension()).isEqualTo("jpg");
    }
}
