package com.flow.fileextension.service;

import com.flow.fileextension.domain.extension.entity.Extension;
import com.flow.fileextension.domain.extension.repository.ExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("파일 검증 서비스 테스트")
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private ExtensionRepository extensionRepository;

    @BeforeEach
    void setUp() {
        extensionRepository.deleteAll();
        
        // 차단된 확장자 설정
        Extension exe = Extension.createFixed("exe");
        exe.block();
        Extension bat = Extension.createFixed("bat");
        bat.block();
        Extension cmd = Extension.createFixed("cmd");
        cmd.block();
        
        extensionRepository.saveAll(List.of(exe, bat, cmd));
    }

    @Test
    @DisplayName("차단된 확장자 파일 - 검증 실패")
    void checkFileExtension_BlockedExtension_ReturnsTrue() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "virus.exe",
                "application/octet-stream",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isTrue(); // 차단됨
    }

    @Test
    @DisplayName("허용된 확장자 파일 - 검증 성공")
    void checkFileExtension_AllowedExtension_ReturnsFalse() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isFalse(); // 허용됨
    }

    @Test
    @DisplayName("이중 확장자 파일 - 검증 실패")
    void checkFileExtension_DoubleExtension_ReturnsTrue() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "virus.exe.txt",
                "text/plain",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isTrue(); // 차단됨 (exe가 포함되어 있음)
    }

    @ParameterizedTest
    @CsvSource({
        "malware.EXE, true",
        "script.Bat, true",
        "file.CMD, true",
        "document.PDF, false",
        "image.JPG, false"
    })
    @DisplayName("대소문자 구분 없이 확장자 검증")
    void checkFileExtension_CaseInsensitive(String filename, boolean expectedBlocked) {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "application/octet-stream",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isEqualTo(expectedBlocked);
    }

    @Test
    @DisplayName("확장자가 없는 파일 - 검증 성공")
    void checkFileExtension_NoExtension_ReturnsFalse() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "README",
                "text/plain",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isFalse(); // 허용됨
    }

    @Test
    @DisplayName("삼중 확장자 파일 - 검증 실패")
    void checkFileExtension_TripleExtension_ReturnsTrue() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.bat.exe.txt",
                "text/plain",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isTrue(); // 차단됨 (bat, exe 포함)
    }

    @Test
    @DisplayName("점으로 시작하는 숨김 파일 처리")
    void checkFileExtension_HiddenFile_HandledCorrectly() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                ".gitignore",
                "text/plain",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isFalse(); // 허용됨
    }

    @Test
    @DisplayName("차단되지 않은 확장자를 가진 파일 - 검증 성공")
    void checkFileExtension_UnblockedCustomExtension_ReturnsFalse() {
        // given
        Extension custom = Extension.createCustom("custom");
        // 차단하지 않음 (isBlocked = false)
        extensionRepository.save(custom);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.custom",
                "application/octet-stream",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isFalse(); // 허용됨
    }

    @Test
    @DisplayName("여러 점이 포함된 파일명 처리")
    void checkFileExtension_MultipleDotsInFilename_HandledCorrectly() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "my.document.v1.0.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // when
        boolean result = fileService.checkFileExtension(file);

        // then
        assertThat(result).isFalse(); // 허용됨
    }
}
