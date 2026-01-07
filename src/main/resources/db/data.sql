-- =========================
-- SUPER_ADMIN 계정
-- =========================
INSERT INTO users (
    login_id,
    password,
    name,
    email,
    phone,
    birthday,
    gender,
    role,
    created_at,
    updated_at,
    deleted_at
) VALUES (
             'superadmin123',
             '$2a$10$vSFHdT6/.UyT2MiB0usJ1erggGvV6MRM86xAEPadiaihhTJtwGXU2',
             '총괄관리자',
             'superadmin@example.com',
             '010-0000-0000',
             '1990-01-01',
             'FEMALE',
             'SUPER_ADMIN',
             NOW(),
             NOW(),
             NULL
         );

-- =========================
-- 추천 시스템 기본 태그
-- =========================

-- 공통 추천 태그
INSERT INTO tags (tag_key, name, pet_type, active) VALUES
                                                       ('ALLERGY_HYPOALLERGENIC', '저알러지', 'BOTH', true),
                                                       ('DIET_WEIGHT_CONTROL',   '체중 관리', 'BOTH', true),
                                                       ('HIGH_CALORIE',          '고칼로리', 'BOTH', true),
                                                       ('NEUTERED_CARE',         '중성화 케어', 'BOTH', true);

-- =========================
-- DOG 생애주기 태그
-- =========================
INSERT INTO tags (tag_key, name, pet_type, active) VALUES
                                                       ('DOG_LIFE_STAGE_PUPPY',  '강아지 퍼피',   'DOG', true),
                                                       ('DOG_LIFE_STAGE_ADULT',  '강아지 어덜트', 'DOG', true),
                                                       ('DOG_LIFE_STAGE_SENIOR', '강아지 시니어', 'DOG', true);

-- =========================
-- CAT 생애주기 태그
-- =========================
INSERT INTO tags (tag_key, name, pet_type, active) VALUES
                                                       ('CAT_LIFE_STAGE_KITTEN', '고양이 키튼',   'CAT', true),
                                                       ('CAT_LIFE_STAGE_ADULT',  '고양이 어덜트', 'CAT', true),
                                                       ('CAT_LIFE_STAGE_SENIOR', '고양이 시니어', 'CAT', true);

-- =========================
-- 사료 / 간식 (종 전용)
-- =========================
INSERT INTO tags (tag_key, name, pet_type, active) VALUES
                                                       ('DOG_FOOD',  '강아지 사료', 'DOG', true),
                                                       ('DOG_SNACK', '강아지 간식', 'DOG', true),
                                                       ('CAT_FOOD',  '고양이 사료', 'CAT', true),
                                                       ('CAT_SNACK', '고양이 간식', 'CAT', true);