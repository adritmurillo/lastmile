ALTER TABLE couriers ADD COLUMN user_id UUID;
UPDATE couriers SET user_id = (SELECT id FROM users WHERE username = 'carlos') WHERE id = 'e8c6d367-abb7-4dab-88b7-efb4a008341c';