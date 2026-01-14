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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Extension(String extension, boolean isFixed, boolean isBlocked, User createdBy) {
        this.extension = extension;
        this.isFixed = isFixed;
        this.isBlocked = isBlocked;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
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

    public void updateBlockStatus(boolean isBlocked, User updatedBy) {
        this.isBlocked = isBlocked;
        this.updatedBy = updatedBy;
    }

    public void updateUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }
}
