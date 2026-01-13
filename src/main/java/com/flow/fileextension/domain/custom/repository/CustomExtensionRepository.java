package com.flow.fileextension.domain.custom.repository;

import com.flow.fileextension.domain.custom.entity.CustomExtension;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomExtensionRepository extends JpaRepository<CustomExtension, Long> {
    boolean existsByExtension(String extension);
    long count();
}
