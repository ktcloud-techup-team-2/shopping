package com.kt.service.pet;

public final class RecommendationTagKeys {
    private RecommendationTagKeys() {}

    // 공통
    public static final String ALLERGY_HYPOALLERGENIC = "ALLERGY_HYPOALLERGENIC";
    public static final String DIET_WEIGHT_CONTROL   = "DIET_WEIGHT_CONTROL";
    public static final String HIGH_CALORIE          = "HIGH_CALORIE";
    public static final String NEUTERED_CARE         = "NEUTERED_CARE";

    // 생애주기(DOG)
    public static final String DOG_LIFE_STAGE_PUPPY  = "DOG_LIFE_STAGE_PUPPY";
    public static final String DOG_LIFE_STAGE_ADULT  = "DOG_LIFE_STAGE_ADULT";
    public static final String DOG_LIFE_STAGE_SENIOR = "DOG_LIFE_STAGE_SENIOR";

    // 생애주기(CAT)
    public static final String CAT_LIFE_STAGE_KITTEN = "CAT_LIFE_STAGE_KITTEN";
    public static final String CAT_LIFE_STAGE_ADULT  = "CAT_LIFE_STAGE_ADULT";
    public static final String CAT_LIFE_STAGE_SENIOR = "CAT_LIFE_STAGE_SENIOR";

    // 사료/간식(종 전용)
    public static final String DOG_FOOD  = "DOG_FOOD";
    public static final String DOG_SNACK = "DOG_SNACK";
    public static final String CAT_FOOD  = "CAT_FOOD";
    public static final String CAT_SNACK = "CAT_SNACK";
}
