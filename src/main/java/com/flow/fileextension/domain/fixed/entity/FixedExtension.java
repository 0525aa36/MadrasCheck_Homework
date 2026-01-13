package com.flow.fileextension.domain.fixed.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fixed_extensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String extension;

    @Column(nullable = false)
    private boolean isBlocked;

    @Builder
    public FixedExtension(String extension, boolean isBlocked) {
        this.extension = extension;
        this.isBlocked = isBlocked;
    }

    public void updateBlockStatus(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}
