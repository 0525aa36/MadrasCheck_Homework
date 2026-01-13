package com.flow.fileextension.domain.fixed.repository;

import com.flow.fileextension.domain.fixed.entity.FixedExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FixedExtensionRepository extends JpaRepository<FixedExtension, Long> {
    Optional<FixedExtension> findByExtension(String extension);
    boolean existsByExtension(String extension);
}
