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