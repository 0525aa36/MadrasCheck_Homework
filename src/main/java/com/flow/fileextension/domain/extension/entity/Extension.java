package com.flow.fileextension.domain.extension.entity;

import com.flow.fileextension.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "extensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Extension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String extension;

    @Column(nullable = false)
    private boolean isFixed;

    @Column(nullable = false)
    private boolean isBlocked;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Extension(String extension, boolean isFixed, boolean isBlocked, Long createdBy) {
        this.extension = extension;
        this.isFixed = isFixed;
        this.isBlocked = isBlocked;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    // 정적 팩토리 메서드 - 테스트용
    public static Extension createFixed(String extension) {
        return Extension.builder()
                .extension(extension)
                .isFixed(true)
                .isBlocked(false)
                .build();
    }

    public static Extension createCustom(String extension) {
        return Extension.builder()
                .extension(extension)
                .isFixed(false)
                .isBlocked(true)
                .build();
    }
    
    // 테스트용 추가 메서드
    @Transactional(readOnly = true)
    public List<Extension> getFixedExtensions() {
        return extensionRepository.findByIsFixedTrue();
    }
    
    @Transactional(readOnly = true)
    public List<Extension> getCustomExtensions() {
        return extensionRepository.findByIsFixedFalse();
    }
    
    @Transactional(readOnly = true)
    public List<String> getBlockedExtensions() {
        return extensionRepository.findByIsBlockedTrue().stream()
                .map(Extension::getExtension)
                .collect(Collectors.toList());
    }
    
    public void updateFixedExtensionBlockStatus(Long id, Boolean isBlocked, Long userId) {
        Extension extension = findExtensionById(id);
        extension.updateBlockStatus(isBlocked, userId);
        extensionRepository.save(extension);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBlockStatus(boolean isBlocked, Long updatedBy) {
        this.isBlocked = isBlocked;
        this.updatedBy = updatedBy;
    }

    public void updateUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    // 편의 메서드
    public void block() {
        this.isBlocked = true;
    }

    public void unblock() {
        this.isBlocked = false;
    }
}
