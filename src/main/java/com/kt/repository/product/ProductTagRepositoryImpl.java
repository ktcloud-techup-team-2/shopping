package com.kt.repository.product;

import com.kt.domain.pet.PetType;
import com.kt.domain.product.ProductStatus;
import com.kt.domain.product.QProduct;
import com.kt.domain.product.QProductTag;
import com.kt.domain.tag.QTag;
import com.kt.dto.product.ProductRecommendRow;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductTagRepositoryImpl implements ProductTagRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductRecommendRow> findRecommendedProductsByTagIds(List<Long> tagIds, PetType petType, Pageable pageable) {
        QProductTag pt = QProductTag.productTag;
        QProduct p = QProduct.product;
        QTag t = QTag.tag;

        // DOG -> DOG+BOTH
        BooleanExpression petTypeCondition =
                petType == PetType.DOG
                        ? p.petType.in(PetType.DOG, PetType.BOTH)
                        : p.petType.in(PetType.CAT, PetType.BOTH);

        List<ProductRecommendRow> content = queryFactory
                .select(Projections.constructor(
                        ProductRecommendRow.class,
                        p.id,
                        p.name,
                        p.price,
                        p.status.stringValue(),
                        p.petType,
                        pt.tag.id.countDistinct()
                ))
                .from(pt)
                .join(pt.product, p)
                .join(pt.tag, t)
                .where(
                        p.deleted.isFalse(),
                        t.deleted.isFalse(),
                        t.active.isTrue(),
                        p.status.eq(ProductStatus.ACTIVE),
                        petTypeCondition,
                        pt.tag.id.in(tagIds)
                )
                .groupBy(p.id, p.name, p.price)
                .orderBy(
                        pt.tag.id.countDistinct().desc(),
                        p.createdAt.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(p.id.countDistinct())
                .from(pt)
                .join(pt.product, p)
                .join(pt.tag, t)
                .where(
                        p.deleted.isFalse(),
                        t.deleted.isFalse(),
                        t.active.isTrue(),
                        p.status.eq(ProductStatus.ACTIVE),
                        petTypeCondition,
                        pt.tag.id.in(tagIds)
                );
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
