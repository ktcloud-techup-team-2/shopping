package com.kt.service.pet;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.pet.BodyShape;
import com.kt.domain.pet.Pet;
import com.kt.domain.pet.PetType;
import com.kt.domain.tag.Tag;
import com.kt.dto.pet.PetRecommendProductResponse;
import com.kt.dto.pet.PetRecommendTagResponse;
import com.kt.dto.product.ProductRecommendRow;
import com.kt.repository.pet.PetRepository;
import com.kt.repository.product.ProductTagRepository;
import com.kt.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetRecommendationService {

    private final PetRepository petRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;

    public PetRecommendTagResponse.Result recommendTags (Long userId, Long petId) {
        Pet pet = petRepository.findByIdAndUser_IdAndDeletedFalse(petId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PET_NOT_FOUND));

        Set<String> keys = resolveRecommendationTagKeys(pet);

        List<Tag> tags = tagRepository.findAllByKeyInAndActiveTrueAndDeletedFalse(keys);

        List<PetRecommendTagResponse.TagItem> items = tags.stream()
                .map(tag -> new PetRecommendTagResponse.TagItem(
                        tag.getId(), tag.getKey(), tag.getName()
                ))
                .toList();

        return new PetRecommendTagResponse.Result(petId, items);
    }

    public Page<PetRecommendProductResponse.ProductItem> recommendProducts (Long userId, Long petId, Pageable pageable) {
        Pet pet = petRepository.findByIdAndUser_IdAndDeletedFalse(petId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PET_NOT_FOUND));

        Set<String> keys = resolveRecommendationTagKeys(pet);
        List<Tag> tags = tagRepository.findAllByKeyInAndActiveTrueAndDeletedFalse(keys);

        if(tags.isEmpty() || keys.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> tagIds = tags.stream().map(Tag::getId).toList();

        Page<ProductRecommendRow> page = productTagRepository.findRecommendedProductsByTagIds(tagIds, pet.getType(), pageable);

        return  page.map(row -> new PetRecommendProductResponse.ProductItem(
                row.productId(),
                row.name(),
                row.price(),
                row.status(),
                row.petType(),
                row.matchedTagCount()
        ));
    }

    private Set<String> resolveRecommendationTagKeys(Pet pet) {     // 펫에 맞는 테그 키 생성
        Set<String> keys = new LinkedHashSet<>();

//        if(pet.getType() == PetType.DOG) {
//            keys.add(RecommendationTagKeys.DOG_FOOD);
//            keys.add(RecommendationTagKeys.DOG_SNACK);
//        } else if(pet.getType() == PetType.CAT) {
//            keys.add(RecommendationTagKeys.CAT_FOOD);
//            keys.add(RecommendationTagKeys.CAT_SNACK);
//        }

        // 알러지
        if (pet.isAllergy()) {
            keys.add(RecommendationTagKeys.ALLERGY_HYPOALLERGENIC);
        }

        // 체형
        if (pet.getBodyShape() == BodyShape.CHUBBY) {
            keys.add(RecommendationTagKeys.DIET_WEIGHT_CONTROL);
        } else if (pet.getBodyShape() == BodyShape.SKINNY) {
            keys.add(RecommendationTagKeys.HIGH_CALORIE);
        }

        // 중성화
        if (pet.isNeutered()) {
            keys.add(RecommendationTagKeys.NEUTERED_CARE);
            keys.add(RecommendationTagKeys.DIET_WEIGHT_CONTROL);
        }
        // 나이
        int age = calculateAge(pet.getBirthday());
        if (pet.getType() == PetType.DOG) {
            keys.add(resolveDogLifeStageKey(age));
        } else if (pet.getType() == PetType.CAT) {
            keys.add(resolveCatLifeStageKey(age));
        }
        return keys;
    }

    private String resolveDogLifeStageKey(int age) {
        if (age < 2) return RecommendationTagKeys.DOG_LIFE_STAGE_PUPPY;
        if (age < 8) return RecommendationTagKeys.DOG_LIFE_STAGE_ADULT;
        return RecommendationTagKeys.DOG_LIFE_STAGE_SENIOR;
    }

    private String resolveCatLifeStageKey(int age) {
        if (age < 1) return RecommendationTagKeys.CAT_LIFE_STAGE_KITTEN;
        if (age < 7) return RecommendationTagKeys.CAT_LIFE_STAGE_ADULT;
        return RecommendationTagKeys.CAT_LIFE_STAGE_SENIOR;
    }

    private int calculateAge(LocalDate birthday) {
        return Period.between(birthday, LocalDate.now()).getYears();
    }
}
