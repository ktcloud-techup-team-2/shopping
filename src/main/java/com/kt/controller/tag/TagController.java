package com.kt.controller.tag;

import com.kt.common.api.ApiResponseEntity;
import com.kt.dto.tag.TagRequest;
import com.kt.dto.tag.TagResponse;
import com.kt.service.tag.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/tags")
public class TagController {
    private final TagService tagService;

    @PostMapping
    public ApiResponseEntity<TagResponse.Detail> create(@Valid @RequestBody TagRequest.Create request) {
        var response = tagService.create(request);
        return ApiResponseEntity.created(response);
    }

    @GetMapping("/{tagId}")
    public ApiResponseEntity<TagResponse.Detail> getTag(@PathVariable Long tagId) {
        var response = tagService.getTag(tagId);
        return ApiResponseEntity.success(response);
    }

    @GetMapping
    public ApiResponseEntity<List<TagResponse.Detail>> getTags(Pageable pageable) {
        var response = tagService.getTags(pageable);
        return ApiResponseEntity.pageOf(response);
    }

    @PatchMapping("/{tagId}")
    public ApiResponseEntity<TagResponse.Detail> update(
            @PathVariable Long tagId,
            @Valid @RequestBody TagRequest.Update request
    ) {
        var response = tagService.update(tagId, request);
        return ApiResponseEntity.success(response);
    }
}
