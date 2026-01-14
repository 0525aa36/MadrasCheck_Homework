package com.flow.fileextension.global.migration;

import com.flow.fileextension.domain.custom.entity.CustomExtension;
import com.flow.fileextension.domain.custom.repository.CustomExtensionRepository;
import com.flow.fileextension.domain.extension.entity.Extension;
import com.flow.fileextension.domain.extension.repository.ExtensionRepository;
import com.flow.fileextension.domain.fixed.entity.FixedExtension;
import com.flow.fileextension.domain.fixed.repository.FixedExtensionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 기존 테이블(fixed_extensions, custom_extensions)의 데이터를 
 * 새로운 통합 테이블(extensions)로 마이그레이션하는 서비스
 * 
 * 실행 순서:
 * 1. 애플리케이션 시작 시 자동 실행
 * 2. 기존 데이터가 있는지 확인
 * 3. 새 테이블에 데이터가 없으면 마이그레이션 수행
 * 4. 완료 후 로그 출력
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test") // 테스트 환경에서는 실행하지 않음
public class DataMigrationService {

    private final FixedExtensionRepository fixedExtensionRepository;
    private final CustomExtensionRepository customExtensionRepository;
    private final ExtensionRepository extensionRepository;

    @PostConstruct
    @Transactional
    public void migrateData() {
        log.info("=== 데이터 마이그레이션 시작 ===");
        
        // 이미 마이그레이션이 완료되었는지 확인
        long extensionCount = extensionRepository.count();
        if (extensionCount > 0) {
            log.info("이미 마이그레이션이 완료되었습니다. (extensions 테이블에 {} 개의 데이터 존재)", extensionCount);
            return;
        }

        int migratedCount = 0;
        
        // 1. 고정 확장자 마이그레이션
        List<FixedExtension> fixedExtensions = fixedExtensionRepository.findAll();
        log.info("고정 확장자 {} 개를 마이그레이션합니다...", fixedExtensions.size());
        
        for (FixedExtension fixed : fixedExtensions) {
            Extension extension = Extension.builder()
                    .extension(fixed.getExtension())
                    .isFixed(true)
                    .isBlocked(fixed.isBlocked())
                    .createdBy(null) // 기존 데이터는 생성자 정보 없음
                    .build();
            extensionRepository.save(extension);
            migratedCount++;
            log.debug("고정 확장자 마이그레이션: {} (차단: {})", fixed.getExtension(), fixed.isBlocked());
        }

        // 2. 커스텀 확장자 마이그레이션
        List<CustomExtension> customExtensions = customExtensionRepository.findAll();
        log.info("커스텀 확장자 {} 개를 마이그레이션합니다...", customExtensions.size());
        
        for (CustomExtension custom : customExtensions) {
            Extension extension = Extension.builder()
                    .extension(custom.getExtension())
                    .isFixed(false)
                    .isBlocked(true) // 커스텀 확장자는 기본적으로 차단됨
                    .createdBy(null) // 기존 데이터는 생성자 정보 없음
                    .build();
            extensionRepository.save(extension);
            migratedCount++;
            log.debug("커스텀 확장자 마이그레이션: {}", custom.getExtension());
        }

        log.info("=== 데이터 마이그레이션 완료 ===");
        log.info("총 {} 개의 확장자가 마이그레이션되었습니다.", migratedCount);
        log.info("- 고정 확장자: {} 개", fixedExtensions.size());
        log.info("- 커스텀 확장자: {} 개", customExtensions.size());
        log.warn("마이그레이션이 완료되었습니다. 이제 기존 테이블(fixed_extensions, custom_extensions)을 삭제해도 됩니다.");
    }
}
