package com.kt.repository.category;

import com.kt.domain.category.Category;
import com.kt.domain.category.QCategory;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.pet.PetType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Category> findAllForAdmin(PetType petType) {
		QCategory category = QCategory.category;
		QCategory parent = new QCategory("parentCategory");

		return queryFactory.selectFrom(category)
			.leftJoin(category.parent, parent).fetchJoin()
			.where(category.deleted.isFalse()
				.and(petType == null ? null : category.petType.eq(petType)))
			.orderBy(category.depth.asc(), category.sortOrder.asc(), category.id.asc())
			.fetch();
	}

	@Override
	public List<Category> findAllByStatusAndPetType(CategoryStatus status, PetType petType) {
		QCategory category = QCategory.category;
		QCategory parent = new QCategory("parentCategory");

		return queryFactory.selectFrom(category)
			.leftJoin(category.parent, parent).fetchJoin()
			.where(
				category.deleted.isFalse()
					.and(category.status.eq(status))
					.and(category.petType.eq(petType))
			)
			.orderBy(category.depth.asc(), category.sortOrder.asc(), category.id.asc())
			.fetch();
	}
}
