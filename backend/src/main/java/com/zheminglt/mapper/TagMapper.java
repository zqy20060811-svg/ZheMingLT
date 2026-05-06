package com.zheminglt.mapper;

import com.zheminglt.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagMapper extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findAllOrderByUsageCountDesc();
    
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %?1% ORDER BY t.usageCount DESC")
    List<Tag> findByNameContaining(String keyword);
}
