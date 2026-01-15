package com.flow.fileextension.domain.extension.repository;

import com.flow.fileextension.domain.extension.entity.Extension;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtensionRepository extends JpaRepository<Extension, Long> {

    // 고정 확장자 목록 조회 (N+1 방지: createdBy, updatedBy eager loading)
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    List<Extension> findByIsFixedTrue();

    // 커스텀 확장자 목록 조회 (N+1 방지: createdBy, updatedBy eager loading)
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    List<Extension> findByIsFixedFalse();

    // 차단된 확장자 목록 조회 (고정 + 커스텀) (N+1 방지: createdBy, updatedBy eager loading)
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    List<Extension> findByIsBlockedTrue();

    // 확장자명으로 조회
    Optional<Extension> findByExtension(String extension);

    // 확장자 존재 여부 확인
    boolean existsByExtension(String extension);

    // 커스텀 확장자 개수 조회
    long countByIsFixedFalse();
}
