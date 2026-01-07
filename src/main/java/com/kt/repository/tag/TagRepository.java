package com.kt.repository.tag;

import com.kt.domain.tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByKeyAndDeletedFalse(String key);
    Optional<Tag> findByIdAndDeletedFalse(Long tagId);

    Page<Tag> findAllByDeletedFalse(Pageable pageable);
}
