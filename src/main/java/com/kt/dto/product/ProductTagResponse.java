package com.kt.dto.product;

import java.util.List;

public class ProductTagResponse {

    public record TagItem(
            Long tagId,
            String tagName
    ) {}

    public record ReplaceResult(
            Long productId,
            List<TagItem> tags
    ) {}
}
