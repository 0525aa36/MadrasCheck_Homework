package com.flow.fileextension.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("확장자 검증 유틸리티 테스트")
class ExtensionValidatorTest {

    @Test
    @DisplayName("유효한 확장자 형식 검증 성공")
    void validateFormat_ValidExtension_Success() {
        // given
        String validExtension = "pdf";
        
        // when & then
        assertThatCode(() -> ExtensionValidator.validateFormat(validExtension))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("빈 문자열이나 공백은 예외 발생")
    void validateFormat_EmptyOrBlank_ThrowsException(String input) {
        // when & then
        assertThatThrownBy(() -> ExtensionValidator.validateFormat(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자는 비어있을 수 없습니다.");
    }

    @Test
    @DisplayName("20자 초과 확장자는 예외 발생")
    void validateFormat_TooLong_ThrowsException() {
        // given
        String tooLongExtension = "a".repeat(21);
        
        // when & then
        assertThatThrownBy(() -> ExtensionValidator.validateFormat(tooLongExtension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자는 최대 20자까지 입력 가능합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"exe!", "pdf@", "txt#", "doc$", "xls%"})
    @DisplayName("특수문자 포함 확장자는 예외 발생")
    void validateFormat_WithSpecialCharacters_ThrowsException(String input) {
        // when & then
        assertThatThrownBy(() -> ExtensionValidator.validateFormat(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자는 영문자와 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("확장자 정규화 - 소문자 변환 및 점 제거")
    void normalizeExtension_Success() {
        // given & when & then
        assertThat(ExtensionValidator.normalizeExtension("PDF")).isEqualTo("pdf");
        assertThat(ExtensionValidator.normalizeExtension(".pdf")).isEqualTo("pdf");
        assertThat(ExtensionValidator.normalizeExtension(".PDF")).isEqualTo("pdf");
        assertThat(ExtensionValidator.normalizeExtension("Pdf")).isEqualTo("pdf");
    }

    @Test
    @DisplayName("차단된 확장자 감지 - 단일 확장자")
    void hasBlockedExtension_SingleExtension_Detected() {
        // given
        String filename = "document.exe";
        Set<String> blockedExtensions = Set.of("exe", "bat", "cmd");
        
        // when
        boolean result = ExtensionValidator.hasBlockedExtension(filename, blockedExtensions);
        
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("차단되지 않은 확장자 감지 실패")
    void hasBlockedExtension_AllowedExtension_NotDetected() {
        // given
        String filename = "document.pdf";
        Set<String> blockedExtensions = Set.of("exe", "bat", "cmd");
        
        // when
        boolean result = ExtensionValidator.hasBlockedExtension(filename, blockedExtensions);
        
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이중 확장자 감지 - file.exe.txt")
    void hasBlockedExtension_DoubleExtension_Detected() {
        // given
        String filename = "virus.exe.txt";
        Set<String> blockedExtensions = Set.of("exe", "bat", "cmd");
        
        // when
        boolean result = ExtensionValidator.hasBlockedExtension(filename, blockedExtensions);
        
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("삼중 확장자 감지 - file.bat.exe.txt")
    void hasBlockedExtension_TripleExtension_Detected() {
        // given
        String filename = "malware.bat.exe.txt";
        Set<String> blockedExtensions = Set.of("exe", "bat", "cmd");
        
        // when
        boolean result = ExtensionValidator.hasBlockedExtension(filename, blockedExtensions);
        
        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "virus.EXE, exe",
        "script.BAT, bat",
        "file.Cmd, cmd",
        "test.ScR, scr"
    })
    @DisplayName("대소문자 구분 없이 차단된 확장자 감지")
    void hasBlockedExtension_CaseInsensitive_Detected(String filename, String blockedExt) {
        // given
        Set<String> blockedExtensions = Set.of(blockedExt);
        
        // when
        boolean result = ExtensionValidator.hasBlockedExtension(filename, blockedExtensions);
        
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("확장자가 없는 파일은 차단되지 않음")
    void hasBlockedExtension_NoExtension_NotDetected() {
        // given
        String filename = "document";
        Set<String> blockedExtensions = Set.of("exe", "bat", "cmd");
        
        // when
        boolean result = ExtensionValidator.hasBlockedExtension(filename, blockedExtensions);
        
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("점으로 시작하는 숨김 파일 처리")
    void hasBlockedExtension_HiddenFile_Handled() {
        // given
        String filename = ".exe";
        Set<String> blockedExtensions = Set.of("exe");
        
        // when
        boolean result = ExtensionValidator.hasBlockedExtension(filename, blockedExtensions);
        
        // then
        assertThat(result).isTrue();
    }
}
