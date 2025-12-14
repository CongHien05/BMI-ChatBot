-- ===================================================================
-- SAMPLE USER DATA FOR user_id = 4
-- Scenario: User wants to GAIN weight (58kg → 65kg target)
-- Ready to use - Just copy and paste into MySQL!
-- ===================================================================

-- ===================================================================
-- STEP 1: DELETE OLD DATA (if exists)
-- ===================================================================

DELETE FROM user_exercise_logs WHERE user_id = 4;
DELETE FROM user_food_logs WHERE user_id = 4;
DELETE FROM body_measurements WHERE user_id = 4;
DELETE FROM user_profiles WHERE user_id = 4;

-- ===================================================================
-- STEP 2: USER PROFILE - Set target weight = 65kg (GAIN WEIGHT)
-- ===================================================================

INSERT INTO user_profiles (user_id, gender, goal_type, goal_weight_kg, daily_calorie_goal) VALUES
(4, 'Nam', 'GAIN_WEIGHT', 65.0, 2500);

-- ===================================================================
-- STEP 3: BODY MEASUREMENTS - 30 days (Weight: 58kg → 58.5kg)
-- Starting weight: 58kg, gradually increasing to test progress bar
-- ===================================================================

INSERT INTO body_measurements (user_id, weight_kg, height_cm, date_recorded) VALUES
-- 30 days ago (oldest)
(4, 58.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 29 DAY)),
(4, 58.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 28 DAY)),
(4, 58.1, 170.0, DATE_SUB(CURDATE(), INTERVAL 27 DAY)),
(4, 58.1, 170.0, DATE_SUB(CURDATE(), INTERVAL 26 DAY)),
(4, 58.2, 170.0, DATE_SUB(CURDATE(), INTERVAL 25 DAY)),
(4, 58.2, 170.0, DATE_SUB(CURDATE(), INTERVAL 24 DAY)),
(4, 58.3, 170.0, DATE_SUB(CURDATE(), INTERVAL 23 DAY)),
(4, 58.3, 170.0, DATE_SUB(CURDATE(), INTERVAL 22 DAY)),
(4, 58.4, 170.0, DATE_SUB(CURDATE(), INTERVAL 21 DAY)),
(4, 58.4, 170.0, DATE_SUB(CURDATE(), INTERVAL 20 DAY)),
(4, 58.4, 170.0, DATE_SUB(CURDATE(), INTERVAL 19 DAY)),
(4, 58.5, 170.0, DATE_SUB(CURDATE(), INTERVAL 18 DAY)),
(4, 58.5, 170.0, DATE_SUB(CURDATE(), INTERVAL 17 DAY)),
(4, 58.5, 170.0, DATE_SUB(CURDATE(), INTERVAL 16 DAY)),
(4, 58.6, 170.0, DATE_SUB(CURDATE(), INTERVAL 15 DAY)),
(4, 58.6, 170.0, DATE_SUB(CURDATE(), INTERVAL 14 DAY)),
(4, 58.6, 170.0, DATE_SUB(CURDATE(), INTERVAL 13 DAY)),
(4, 58.7, 170.0, DATE_SUB(CURDATE(), INTERVAL 12 DAY)),
(4, 58.7, 170.0, DATE_SUB(CURDATE(), INTERVAL 11 DAY)),
(4, 58.7, 170.0, DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
(4, 58.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 9 DAY)),
(4, 58.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 8 DAY)),
(4, 58.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
(4, 58.9, 170.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(4, 58.9, 170.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(4, 58.9, 170.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(4, 59.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(4, 59.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(4, 59.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
-- Today (current weight: 58kg for testing, or 59.0 if you want to show progress)
(4, 58.0, 170.0, CURDATE());

-- ===================================================================
-- STEP 4: FOOD LOGS - 30 days (High calorie for weight gain)
-- ===================================================================

-- Week 1: High calorie meals
INSERT INTO user_food_logs (user_id, food_id, date_eaten, meal_type, quantity) VALUES
-- Day 29 ago
(4, 1, DATE_SUB(NOW(), INTERVAL 29 DAY), 'BREAKFAST', 1.0),   -- Phở bò 350
(4, 5, DATE_SUB(NOW(), INTERVAL 29 DAY), 'LUNCH', 1.0),       -- Cơm tấm 500
(4, 6, DATE_SUB(NOW(), INTERVAL 29 DAY), 'DINNER', 1.0),     -- Cơm gà 450
(4, 32, DATE_SUB(NOW(), INTERVAL 29 DAY), 'SNACK', 2.0),      -- Chuối 210

-- Day 28 ago
(4, 7, DATE_SUB(NOW(), INTERVAL 28 DAY), 'BREAKFAST', 1.0),   -- Bánh mì 350
(4, 4, DATE_SUB(NOW(), INTERVAL 28 DAY), 'LUNCH', 1.0),       -- Bún chả 450
(4, 10, DATE_SUB(NOW(), INTERVAL 28 DAY), 'DINNER', 1.0),     -- Mì xào 550
(4, 33, DATE_SUB(NOW(), INTERVAL 28 DAY), 'SNACK', 1.0),      -- Táo 95

-- Day 27 ago
(4, 2, DATE_SUB(NOW(), INTERVAL 27 DAY), 'BREAKFAST', 1.0),   -- Phở gà 300
(4, 5, DATE_SUB(NOW(), INTERVAL 27 DAY), 'LUNCH', 1.0),       -- Cơm tấm 500
(4, 11, DATE_SUB(NOW(), INTERVAL 27 DAY), 'DINNER', 2.0),     -- Cơm 400
(4, 15, DATE_SUB(NOW(), INTERVAL 27 DAY), 'DINNER', 1.0),     -- Thịt lợn 250
(4, 31, DATE_SUB(NOW(), INTERVAL 27 DAY), 'SNACK', 1.0),      -- Yaourt 100

-- Day 26 ago
(4, 1, DATE_SUB(NOW(), INTERVAL 26 DAY), 'BREAKFAST', 1.0),   -- Phở bò 350
(4, 6, DATE_SUB(NOW(), INTERVAL 26 DAY), 'LUNCH', 1.0),       -- Cơm gà 450
(4, 10, DATE_SUB(NOW(), INTERVAL 26 DAY), 'DINNER', 1.0),     -- Mì xào 550
(4, 25, DATE_SUB(NOW(), INTERVAL 26 DAY), 'BREAKFAST', 1.0),  -- Cà phê sữa 150

-- Day 25 ago
(4, 7, DATE_SUB(NOW(), INTERVAL 25 DAY), 'BREAKFAST', 1.0),   -- Bánh mì 350
(4, 4, DATE_SUB(NOW(), INTERVAL 25 DAY), 'LUNCH', 1.0),       -- Bún chả 450
(4, 5, DATE_SUB(NOW(), INTERVAL 25 DAY), 'DINNER', 1.0),      -- Cơm tấm 500
(4, 34, DATE_SUB(NOW(), INTERVAL 25 DAY), 'SNACK', 1.0),      -- Cam 62

-- Day 24 ago
(4, 2, DATE_SUB(NOW(), INTERVAL 24 DAY), 'BREAKFAST', 1.0),   -- Phở gà 300
(4, 1, DATE_SUB(NOW(), INTERVAL 24 DAY), 'LUNCH', 1.0),      -- Phở bò 350
(4, 11, DATE_SUB(NOW(), INTERVAL 24 DAY), 'DINNER', 2.0),    -- Cơm 400
(4, 16, DATE_SUB(NOW(), INTERVAL 24 DAY), 'DINNER', 1.0),     -- Thịt bò 200
(4, 32, DATE_SUB(NOW(), INTERVAL 24 DAY), 'SNACK', 2.0),     -- Chuối 210

-- Day 23 ago
(4, 7, DATE_SUB(NOW(), INTERVAL 23 DAY), 'BREAKFAST', 1.0),   -- Bánh mì 350
(4, 5, DATE_SUB(NOW(), INTERVAL 23 DAY), 'LUNCH', 1.0),      -- Cơm tấm 500
(4, 6, DATE_SUB(NOW(), INTERVAL 23 DAY), 'DINNER', 1.0),     -- Cơm gà 450
(4, 33, DATE_SUB(NOW(), INTERVAL 23 DAY), 'SNACK', 1.0);      -- Táo 95

-- Continue for remaining days (simplified - you can expand)
-- Days 22-1: Similar pattern
INSERT INTO user_food_logs (user_id, food_id, date_eaten, meal_type, quantity) VALUES
-- Day 22-15 (8 days)
(4, 1, DATE_SUB(NOW(), INTERVAL 22 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 22 DAY), 'LUNCH', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 22 DAY), 'DINNER', 1.0),
(4, 2, DATE_SUB(NOW(), INTERVAL 21 DAY), 'BREAKFAST', 1.0),
(4, 4, DATE_SUB(NOW(), INTERVAL 21 DAY), 'LUNCH', 1.0),
(4, 10, DATE_SUB(NOW(), INTERVAL 21 DAY), 'DINNER', 1.0),
(4, 7, DATE_SUB(NOW(), INTERVAL 20 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 20 DAY), 'LUNCH', 1.0),
(4, 11, DATE_SUB(NOW(), INTERVAL 20 DAY), 'DINNER', 2.0),
(4, 1, DATE_SUB(NOW(), INTERVAL 19 DAY), 'BREAKFAST', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 19 DAY), 'LUNCH', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 19 DAY), 'DINNER', 1.0),
(4, 2, DATE_SUB(NOW(), INTERVAL 18 DAY), 'BREAKFAST', 1.0),
(4, 4, DATE_SUB(NOW(), INTERVAL 18 DAY), 'LUNCH', 1.0),
(4, 10, DATE_SUB(NOW(), INTERVAL 18 DAY), 'DINNER', 1.0),
(4, 7, DATE_SUB(NOW(), INTERVAL 17 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 17 DAY), 'LUNCH', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 17 DAY), 'DINNER', 1.0),
(4, 1, DATE_SUB(NOW(), INTERVAL 16 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 16 DAY), 'LUNCH', 1.0),
(4, 11, DATE_SUB(NOW(), INTERVAL 16 DAY), 'DINNER', 2.0),
(4, 2, DATE_SUB(NOW(), INTERVAL 15 DAY), 'BREAKFAST', 1.0),
(4, 4, DATE_SUB(NOW(), INTERVAL 15 DAY), 'LUNCH', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 15 DAY), 'DINNER', 1.0);

-- Days 14-1 (recent days)
INSERT INTO user_food_logs (user_id, food_id, date_eaten, meal_type, quantity) VALUES
(4, 7, DATE_SUB(NOW(), INTERVAL 14 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 14 DAY), 'LUNCH', 1.0),
(4, 10, DATE_SUB(NOW(), INTERVAL 14 DAY), 'DINNER', 1.0),
(4, 1, DATE_SUB(NOW(), INTERVAL 13 DAY), 'BREAKFAST', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 13 DAY), 'LUNCH', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 13 DAY), 'DINNER', 1.0),
(4, 2, DATE_SUB(NOW(), INTERVAL 12 DAY), 'BREAKFAST', 1.0),
(4, 4, DATE_SUB(NOW(), INTERVAL 12 DAY), 'LUNCH', 1.0),
(4, 11, DATE_SUB(NOW(), INTERVAL 12 DAY), 'DINNER', 2.0),
(4, 7, DATE_SUB(NOW(), INTERVAL 11 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 11 DAY), 'LUNCH', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 11 DAY), 'DINNER', 1.0),
(4, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 10 DAY), 'LUNCH', 1.0),
(4, 10, DATE_SUB(NOW(), INTERVAL 10 DAY), 'DINNER', 1.0),
(4, 2, DATE_SUB(NOW(), INTERVAL 9 DAY), 'BREAKFAST', 1.0),
(4, 4, DATE_SUB(NOW(), INTERVAL 9 DAY), 'LUNCH', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 9 DAY), 'DINNER', 1.0),
(4, 7, DATE_SUB(NOW(), INTERVAL 8 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 8 DAY), 'LUNCH', 1.0),
(4, 11, DATE_SUB(NOW(), INTERVAL 8 DAY), 'DINNER', 2.0),
(4, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), 'BREAKFAST', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 7 DAY), 'LUNCH', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 7 DAY), 'DINNER', 1.0),
(4, 2, DATE_SUB(NOW(), INTERVAL 6 DAY), 'BREAKFAST', 1.0),
(4, 4, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),
(4, 10, DATE_SUB(NOW(), INTERVAL 6 DAY), 'DINNER', 1.0),
(4, 7, DATE_SUB(NOW(), INTERVAL 5 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 5 DAY), 'LUNCH', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 5 DAY), 'DINNER', 1.0),
(4, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 4 DAY), 'LUNCH', 1.0),
(4, 11, DATE_SUB(NOW(), INTERVAL 4 DAY), 'DINNER', 2.0),
(4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 'BREAKFAST', 1.0),
(4, 4, DATE_SUB(NOW(), INTERVAL 3 DAY), 'LUNCH', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DINNER', 1.0),
(4, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), 'BREAKFAST', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 'LUNCH', 1.0),
(4, 10, DATE_SUB(NOW(), INTERVAL 2 DAY), 'DINNER', 1.0),
(4, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'BREAKFAST', 1.0),
(4, 6, DATE_SUB(NOW(), INTERVAL 1 DAY), 'LUNCH', 1.0),
(4, 5, DATE_SUB(NOW(), INTERVAL 1 DAY), 'DINNER', 1.0),
-- Today
(4, 2, CURDATE(), 'BREAKFAST', 1.0),
(4, 5, CURDATE(), 'LUNCH', 1.0),
(4, 6, CURDATE(), 'DINNER', 1.0);

-- ===================================================================
-- STEP 5: EXERCISE LOGS - 30 days (Moderate exercise for muscle gain)
-- ===================================================================

INSERT INTO user_exercise_logs (user_id, exercise_id, date_exercised, duration_minutes) VALUES
-- Week 1-4: Regular workouts
(4, 1, DATE_SUB(NOW(), INTERVAL 29 DAY), 30),   -- Chạy bộ
(4, 12, DATE_SUB(NOW(), INTERVAL 28 DAY), 20),  -- Push-up
(4, 3, DATE_SUB(NOW(), INTERVAL 27 DAY), 45),   -- Đạp xe
(4, 1, DATE_SUB(NOW(), INTERVAL 26 DAY), 25),   -- Chạy bộ
(4, 12, DATE_SUB(NOW(), INTERVAL 25 DAY), 15),  -- Push-up
(4, 2, DATE_SUB(NOW(), INTERVAL 24 DAY), 40),   -- Đi bộ nhanh
(4, 1, DATE_SUB(NOW(), INTERVAL 23 DAY), 30),   -- Chạy bộ
(4, 3, DATE_SUB(NOW(), INTERVAL 22 DAY), 45),   -- Đạp xe
(4, 12, DATE_SUB(NOW(), INTERVAL 21 DAY), 20),  -- Push-up
(4, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), 25),   -- Chạy bộ
(4, 2, DATE_SUB(NOW(), INTERVAL 19 DAY), 40),   -- Đi bộ nhanh
(4, 12, DATE_SUB(NOW(), INTERVAL 18 DAY), 15),  -- Push-up
(4, 1, DATE_SUB(NOW(), INTERVAL 17 DAY), 30),   -- Chạy bộ
(4, 3, DATE_SUB(NOW(), INTERVAL 16 DAY), 45),   -- Đạp xe
(4, 12, DATE_SUB(NOW(), INTERVAL 15 DAY), 20),  -- Push-up
(4, 1, DATE_SUB(NOW(), INTERVAL 14 DAY), 25),   -- Chạy bộ
(4, 2, DATE_SUB(NOW(), INTERVAL 13 DAY), 40),   -- Đi bộ nhanh
(4, 12, DATE_SUB(NOW(), INTERVAL 12 DAY), 15),  -- Push-up
(4, 1, DATE_SUB(NOW(), INTERVAL 11 DAY), 30),   -- Chạy bộ
(4, 3, DATE_SUB(NOW(), INTERVAL 10 DAY), 45),   -- Đạp xe
(4, 12, DATE_SUB(NOW(), INTERVAL 9 DAY), 20),  -- Push-up
(4, 1, DATE_SUB(NOW(), INTERVAL 8 DAY), 25),   -- Chạy bộ
(4, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), 40),   -- Đi bộ nhanh
(4, 12, DATE_SUB(NOW(), INTERVAL 6 DAY), 15),  -- Push-up
(4, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 30),   -- Chạy bộ
(4, 3, DATE_SUB(NOW(), INTERVAL 4 DAY), 45),   -- Đạp xe
(4, 12, DATE_SUB(NOW(), INTERVAL 3 DAY), 20),  -- Push-up
(4, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 25),   -- Chạy bộ
(4, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 40),   -- Đi bộ nhanh
(4, 12, CURDATE(), 15);                        -- Push-up

-- ===================================================================
-- VERIFY DATA
-- ===================================================================

-- Check inserted data:
SELECT COUNT(*) as measurement_count FROM body_measurements WHERE user_id = 4;
-- Expected: 30

SELECT COUNT(*) as food_log_count FROM user_food_logs WHERE user_id = 4;
-- Expected: ~90+

SELECT COUNT(*) as exercise_log_count FROM user_exercise_logs WHERE user_id = 4;
-- Expected: 29

-- Check profile:
SELECT * FROM user_profiles WHERE user_id = 4;
-- Expected: goal_weight_kg = 65.0, daily_calorie_goal = 2500

-- View weight trend:
SELECT date_recorded, weight_kg, height_cm 
FROM body_measurements 
WHERE user_id = 4 
ORDER BY date_recorded DESC
LIMIT 10;

-- ===================================================================
-- DONE! Data imported for user_id = 4
-- Current weight: 58kg, Target: 65kg (GAIN WEIGHT)
-- Progress bar should show: 0% hoàn thành • Còn tăng 7.0 kg
-- ===================================================================
