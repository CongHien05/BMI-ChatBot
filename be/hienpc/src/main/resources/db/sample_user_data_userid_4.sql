-- ===================================================================
-- SAMPLE USER DATA FOR user_id = 4
-- Ready to use - Just copy and paste into MySQL!
-- ===================================================================

-- ===================================================================
-- 1. BODY MEASUREMENTS - 7 days (Weight: 70kg → 68.5kg)
-- ===================================================================

INSERT INTO body_measurements (user_id, weight_kg, height_cm, date_recorded) VALUES
(4, 70.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(4, 69.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(4, 69.5, 170.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(4, 69.3, 170.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(4, 69.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(4, 68.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(4, 68.5, 170.0, CURDATE());

-- ===================================================================
-- 2. FOOD LOGS - 7 days (20 entries)
-- ===================================================================

-- Day 6 ago: 1215 kcal (Phở bò + Cơm + Gà luộc + Cơm tấm)
INSERT INTO user_food_logs (user_id, food_id, date_eaten, meal_type, quantity) VALUES
(4, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 'BREAKFAST', 1.0),   -- Phở bò 350
(4, 11, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),      -- Cơm trắng 200
(4, 18, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),      -- Gà luộc 165
(4, 5, DATE_SUB(NOW(), INTERVAL 6 DAY), 'DINNER', 1.0),      -- Cơm tấm 500

-- Day 5 ago: 1105 kcal (Bánh mì + Bún + Thịt bò + Phở gà + Chuối)
(4, 7, DATE_SUB(NOW(), INTERVAL 5 DAY), 'BREAKFAST', 1.0),   -- Bánh mì 350
(4, 12, DATE_SUB(NOW(), INTERVAL 5 DAY), 'LUNCH', 1.0),      -- Bún 150
(4, 16, DATE_SUB(NOW(), INTERVAL 5 DAY), 'LUNCH', 1.0),      -- Thịt bò 200
(4, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 'DINNER', 1.0),      -- Phở gà 300
(4, 32, DATE_SUB(NOW(), INTERVAL 5 DAY), 'SNACK', 1.0),      -- Chuối 105

-- Day 4 ago: 890 kcal (Low calorie day - Phở gà + Salad + Gà + Cơm + Rau + Táo)
(4, 2, DATE_SUB(NOW(), INTERVAL 4 DAY), 'BREAKFAST', 1.0),   -- Phở gà 300
(4, 23, DATE_SUB(NOW(), INTERVAL 4 DAY), 'LUNCH', 1.0),      -- Salad 80
(4, 18, DATE_SUB(NOW(), INTERVAL 4 DAY), 'LUNCH', 1.0),      -- Gà luộc 165
(4, 11, DATE_SUB(NOW(), INTERVAL 4 DAY), 'DINNER', 1.0),     -- Cơm 200
(4, 21, DATE_SUB(NOW(), INTERVAL 4 DAY), 'DINNER', 1.0),     -- Rau xào 50
(4, 33, DATE_SUB(NOW(), INTERVAL 4 DAY), 'SNACK', 1.0),      -- Táo 95

-- Day 3 ago: 1280 kcal (Phở bò + Bún chả + Cơm + Cá + Yaourt)
(4, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'BREAKFAST', 1.0),   -- Phở bò 350
(4, 4, DATE_SUB(NOW(), INTERVAL 3 DAY), 'LUNCH', 1.0),       -- Bún chả 450
(4, 11, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DINNER', 1.0),     -- Cơm 200
(4, 17, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DINNER', 1.0),     -- Cá kho 180
(4, 31, DATE_SUB(NOW(), INTERVAL 3 DAY), 'SNACK', 1.0),      -- Yaourt 100

-- Day 2 ago: 1450 kcal (Higher calorie day - Bánh mì + Cà phê + Cơm tấm + Cơm + Thịt)
(4, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), 'BREAKFAST', 1.0),   -- Bánh mì 350
(4, 25, DATE_SUB(NOW(), INTERVAL 2 DAY), 'BREAKFAST', 1.0),  -- Cà phê sữa 150
(4, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 'LUNCH', 1.0),       -- Cơm tấm 500
(4, 11, DATE_SUB(NOW(), INTERVAL 2 DAY), 'DINNER', 1.0),     -- Cơm 200
(4, 15, DATE_SUB(NOW(), INTERVAL 2 DAY), 'DINNER', 1.0),     -- Thịt lợn 250

-- Day 1 ago: 1042 kcal (Phở gà + Cơm gà + Canh + Cơm + Cam)
(4, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 'BREAKFAST', 1.0),   -- Phở gà 300
(4, 6, DATE_SUB(NOW(), INTERVAL 1 DAY), 'LUNCH', 1.0),       -- Cơm gà 450
(4, 22, DATE_SUB(NOW(), INTERVAL 1 DAY), 'DINNER', 1.0),     -- Canh rau 30
(4, 11, DATE_SUB(NOW(), INTERVAL 1 DAY), 'DINNER', 1.0),     -- Cơm 200
(4, 34, DATE_SUB(NOW(), INTERVAL 1 DAY), 'SNACK', 1.0),      -- Cam 62

-- Today: 875 kcal so far (Phở bò + Nước cam + Cơm + Gà + Rau)
(4, 1, CURDATE(), 'BREAKFAST', 1.0),                          -- Phở bò 350
(4, 27, CURDATE(), 'BREAKFAST', 1.0),                         -- Nước cam 110
(4, 11, CURDATE(), 'LUNCH', 1.0),                             -- Cơm 200
(4, 18, CURDATE(), 'LUNCH', 1.0),                             -- Gà luộc 165
(4, 21, CURDATE(), 'LUNCH', 1.0);                             -- Rau xào 50

-- ===================================================================
-- 3. EXERCISE LOGS - 7 days (8 workouts)
-- ===================================================================

INSERT INTO user_exercise_logs (user_id, exercise_id, date_exercised, duration_minutes) VALUES
-- Day 6 ago: Chạy bộ 30 phút (300 kcal burned)
(4, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 30.0),

-- Day 5 ago: Đạp xe 45 phút (375 kcal burned)
(4, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), 45.0),

-- Day 4 ago: Yoga 60 phút (180 kcal burned)
(4, 18, DATE_SUB(NOW(), INTERVAL 4 DAY), 60.0),

-- Day 3 ago: Chạy bộ 25 phút + Push-up 15 phút
(4, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 25.0),
(4, 12, DATE_SUB(NOW(), INTERVAL 3 DAY), 15.0),

-- Day 2 ago: Nhảy dây 20 phút (233 kcal burned)
(4, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 20.0),

-- Day 1 ago: Đi bộ nhanh 40 phút (200 kcal burned)
(4, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 40.0),

-- Today: Cầu lông 30 phút (225 kcal burned)
(4, 15, CURDATE(), 30.0);

-- ===================================================================
-- VERIFY DATA
-- ===================================================================

-- Check inserted data:
SELECT COUNT(*) as measurement_count FROM body_measurements WHERE user_id = 4;
-- Expected: 7

SELECT COUNT(*) as food_log_count FROM user_food_logs WHERE user_id = 4;
-- Expected: 35

SELECT COUNT(*) as exercise_log_count FROM user_exercise_logs WHERE user_id = 4;
-- Expected: 8

-- View weight trend:
SELECT date_recorded, weight_kg, height_cm 
FROM body_measurements 
WHERE user_id = 4 
ORDER BY date_recorded;

-- View today's food logs:
SELECT f.food_name, ufl.meal_type, ufl.quantity, f.calories_per_unit
FROM user_food_logs ufl
JOIN foods f ON ufl.food_id = f.food_id
WHERE ufl.user_id = 4 AND DATE(ufl.date_eaten) = CURDATE()
ORDER BY ufl.date_eaten;

-- View today's exercise logs:
SELECT e.exercise_name, uel.duration_minutes, e.calories_burned_per_hour
FROM user_exercise_logs uel
JOIN exercises e ON uel.exercise_id = e.exercise_id
WHERE uel.user_id = 4 AND DATE(uel.date_exercised) = CURDATE()
ORDER BY uel.date_exercised;

-- ===================================================================
-- DONE! Data imported for user_id = 4
-- Now open your app and check Dashboard with beautiful charts! 📊
-- ===================================================================

