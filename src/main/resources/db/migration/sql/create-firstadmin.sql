INSERT INTO users (id, login, password_hash, email, first_name, last_name, role, created_at, updated_at)
VALUES (gen_random_uuid(),
        'admin',
        '$2a$12$pgF2BF14CvQimxDhJ8QDzeAqzs4dJOpLV3av7I/tQMPYstQ2NefCG', -- BCrypt("Admin123!")
        'admin@bankcards.local',
        'System',
        'Administrator',
        'ADMIN',
        NOW(),
        NOW());