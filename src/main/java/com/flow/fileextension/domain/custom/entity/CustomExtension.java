package com.flow.fileextension.domain.custom.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_extensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String extension;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CustomExtension(String extension) {
        this.extension = extension;
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
