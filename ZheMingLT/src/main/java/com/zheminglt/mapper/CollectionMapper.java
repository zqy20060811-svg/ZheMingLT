package com.zheminglt.mapper;

import com.zheminglt.model.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionMapper extends JpaRepository<Collection, Long> {
    Optional<Collection> findByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
    Page<Collection> findByUserId(Long userId, Pageable pageable);
    long countByPostId(Long postId);
}