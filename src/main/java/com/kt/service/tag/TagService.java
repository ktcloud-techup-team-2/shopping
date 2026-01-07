package com.kt.service.tag;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.tag.Tag;
import com.kt.dto.tag.TagRequest;
import com.kt.dto.tag.TagResponse;
import com.kt.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public TagResponse.Detail create(TagRequest.Create request) {
        Preconditions.validate(!tagRepository.existsByKeyAndDeletedFalse(request.key()), ErrorCode.TAG_KEY_DUPLICATED);

        Tag tag = Tag.create(request.key(), request.name(), request.petType());
        Tag saved = tagRepository.save(tag);
        return TagResponse.Detail.from(saved);
    }

    @Transactional(readOnly = true)
    public TagResponse.Detail getTag(Long tagId) {
        Tag tag = tagRepository.findByIdAndDeletedFalse(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));
        return TagResponse.Detail.from(tag);
    }

    @Transactional(readOnly = true)
    public Page<TagResponse.Detail> getTags(Pageable pageable) {
        return tagRepository.findAllByDeletedFalse(pageable)
                .map(TagResponse.Detail::from);
    }

    public TagResponse.Detail update(Long tagId, TagRequest.Update request) {
        Tag tag = tagRepository.findByIdAndDeletedFalse(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));
        tag.update(request.key(), request.name(), request.petType());
        if (request.active() != null) {
            if (request.active()) tag.activate();
            else tag.deactivate();
        }
        return TagResponse.Detail.from(tag);
    }

}
