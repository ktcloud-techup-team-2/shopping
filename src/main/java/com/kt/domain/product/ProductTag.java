package com.kt.domain.product;

import com.kt.common.jpa.BaseAuditEntity;
import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name="product_tags",
        uniqueConstraints = @UniqueConstraint(name="uk_product_tag", columnNames={"product_id","tag_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductTag extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tag_id", nullable=false)
    private Tag tag;

    private ProductTag(Product product, Tag tag) {
        this.product = product;
        this.tag = tag;
    }

    public static ProductTag create(Product product, Tag tag) {
        return new ProductTag(product, tag);
    }
}
