-- ===================================================================
-- SAMPLE DATA FOR BMI CHATBOT PROJECT
-- Run after initial setup to populate database with test data
-- ===================================================================

-- Clear existing data (optional - uncomment if needed)
-- DELETE FROM user_exercise_logs;
-- DELETE FROM user_food_logs;
-- DELETE FROM body_measurements;
-- DELETE FROM chat_messages;
-- DELETE FROM chat_conversations;
-- DELETE FROM chatbot_rules WHERE created_by_user_id IS NULL;
-- DELETE FROM exercises;
-- DELETE FROM foods;

-- ===================================================================
-- 1. FOODS - Món ăn phổ biến Việt Nam
-- ===================================================================

INSERT INTO foods (food_name, serving_unit, calories_per_unit) VALUES
-- Món chính
('Phở bò', 'tô', 350),
('Phở gà', 'tô', 300),
('Bún bò Huế', 'tô', 400),
('Bún chả', 'suất', 450),
('Cơm tấm', 'đĩa', 500),
('Cơm gà', 'đĩa', 450),
('Bánh mì thịt', 'ổ', 350),
('Bánh mì pate', 'ổ', 300),
('Mì xào hải sản', 'đĩa', 550),
('Hủ tiếu Nam Vang', 'tô', 380),

-- Cơm/bún
('Cơm trắng', 'chén', 200),
('Bún', 'chén', 150),
('Mì trứng', 'suất', 220),
('Phở khô', 'đĩa', 250),

-- Thịt/cá
('Thịt lợn nướng', '100g', 250),
('Thịt bò xào', '100g', 200),
('Cá kho', '100g', 180),
('Gà luộc', '100g', 165),
('Trứng gà chiên', 'quả', 90),
('Tôm luộc', '100g', 99),

-- Rau củ
('Rau xào', 'đĩa', 50),
('Canh rau', 'tô', 30),
('Salad trộn', 'đĩa', 80),
('Dưa chuột', '100g', 16),

-- Đồ uống
('Cà phê sữa', 'ly', 150),
('Trà đá', 'ly', 0),
('Nước cam', 'ly', 110),
('Sữa tươi', 'hộp 250ml', 120),
('Nước suối', 'chai', 0),

-- Snack/Tráng miệng
('Bánh ngọt', 'cái', 250),
('Yaourt', 'hộp', 100),
('Chuối', 'quả', 105),
('Táo', 'quả', 95),
('Cam', 'quả', 62),

-- Fast food
('Gà rán KFC', 'miếng', 290),
('Burger bò', 'cái', 540),
('Pizza phô mai', 'miếng', 285),
('Khoai tây chiên', 'suất', 365);

-- ===================================================================
-- 2. EXERCISES - Bài tập phổ biến
-- ===================================================================

INSERT INTO exercises (exercise_name, calories_burned_per_hour) VALUES
-- Cardio
('Chạy bộ', 600),
('Đi bộ nhanh', 300),
('Đạp xe', 500),
('Bơi lội', 450),
('Nhảy dây', 700),
('Leo cầu thang', 550),

-- Gym
('Chạy máy treadmill', 600),
('Đạp xe đạp tĩnh', 400),
('Tập tạ', 350),
('Squat', 400),
('Plank', 300),
('Push-up', 350),
('Sit-up', 300),

-- Thể thao
('Đá bóng', 500),
('Bóng chuyền', 360),
('Cầu lông', 450),
('Tennis', 420),
('Bóng rổ', 480),

-- Yoga/Nhẹ
('Yoga', 180),
('Thái cực quyền', 240),
('Aerobic', 400),
('Zumba', 450),

-- Thể dục nhịp điệu
('Jumping Jack', 500),
('Burpee', 600),
('Mountain Climber', 550);

-- ===================================================================
-- 3. CHATBOT RULES - Các câu hỏi thường gặp
-- ===================================================================

INSERT INTO chatbot_rules (intent, keywords, response_template, priority, created_by_user_id) VALUES
-- Chào hỏi
('greeting', 'chào,hello,hi,xin chào,hey', 'Xin chào! 👋 Tôi là BMI Assistant. Tôi có thể giúp gì cho bạn hôm nay?', 10, NULL),

-- BMI
('bmi_info', 'bmi,chỉ số bmi,bmi là gì,tính bmi', 'BMI (Body Mass Index) là chỉ số khối cơ thể, được tính bằng công thức: BMI = Cân nặng (kg) / [Chiều cao (m)]²\n\nPhân loại:\n- < 18.5: Thiếu cân\n- 18.5-24.9: Bình thường\n- 25-29.9: Thừa cân\n- ≥ 30: Béo phì', 5, NULL),

-- Giảm cân
('weight_loss', 'giảm cân,làm sao để giảm,muốn giảm,cách giảm cân,giảm mỡ', 'Để giảm cân hiệu quả:\n\n1. 🍽️ Tạo calorie deficit (ăn ít hơn tiêu thụ)\n2. 💪 Tập luyện đều đặn 3-4 lần/tuần\n3. 💧 Uống đủ nước (2-3 lít/ngày)\n4. 😴 Ngủ đủ giấc (7-8 tiếng)\n5. 🥗 Ăn nhiều rau xanh, protein\n\nGiảm 0.5-1kg/tuần là tốc độ an toàn nhất!', 8, NULL),

-- Tăng cân
('weight_gain', 'tăng cân,làm sao để tăng,muốn tăng,cách tăng cân', 'Để tăng cân lành mạnh:\n\n1. 🍽️ Tạo calorie surplus (ăn nhiều hơn tiêu thụ)\n2. 💪 Tập gym, tập tạ để tăng cơ\n3. 🥩 Tăng protein: thịt, cá, trứng, sữa\n4. 🍚 Ăn nhiều bữa nhỏ trong ngày\n5. 😴 Nghỉ ngơi đầy đủ\n\nTăng 0.25-0.5kg/tuần là hợp lý!', 8, NULL),

-- Món ăn
('food_suggestion', 'nên ăn gì,món ăn nào,ăn gì để,thực đơn', 'Dựa vào mục tiêu của bạn:\n\n🔻 Giảm cân:\n- Phở không mỡ, salad, rau xào\n- Gà luộc, cá hấp\n- Trái cây: táo, cam, chuối\n\n🔺 Tăng cân:\n- Cơm tấm, bún bò, mì xào\n- Thịt bò, thịt lợn, trứng\n- Sữa, yaourt, các loại hạt\n\nBạn có thể log món ăn trong app để theo dõi calories!', 7, NULL),

-- Bài tập
('exercise_suggestion', 'tập gì,bài tập nào,tập thể dục,workout,gym', 'Gợi ý bài tập:\n\n🏃 Cardio (giảm mỡ):\n- Chạy bộ: 30-45 phút\n- Nhảy dây: 15-20 phút\n- Đạp xe: 45-60 phút\n\n💪 Strength (tăng cơ):\n- Squat, Push-up, Plank\n- Tập tạ với các nhóm cơ\n\n🧘 Flexibility:\n- Yoga, stretching\n\nNên tập 3-5 lần/tuần, mỗi lần 30-60 phút!', 7, NULL),

-- Động lực
('motivation', 'chán,mệt,khó quá,không muốn,bỏ cuộc,nản', 'Đừng bỏ cuộc! 💪\n\n✨ Nhớ rằng:\n- Mọi tiến bộ đều có giá trị\n- Thất bại là bước đệm thành công\n- Kiên trì sẽ có kết quả\n- Bạn không cô đơn trên hành trình này\n\n🎯 Tips:\n1. Đặt mục tiêu nhỏ\n2. Tự thưởng khi đạt được\n3. Tìm bạn đồng hành\n4. Nhìn lại progress đã đạt được\n\nBạn làm được! Keep going! 🔥', 6, NULL),

-- Calories
('calories_info', 'calories,calo,năng lượng', 'Calories là đơn vị đo năng lượng từ thực phẩm.\n\n📊 Nhu cầu trung bình:\n- Nam: 2000-2500 kcal/ngày\n- Nữ: 1600-2000 kcal/ngày\n\n🎯 Để:\n- Giảm cân: -300 đến -500 kcal\n- Duy trì: bằng nhu cầu\n- Tăng cân: +300 đến +500 kcal\n\nLog món ăn để theo dõi chính xác!', 6, NULL);

-- ===================================================================
-- 4. SAMPLE USER DATA (for testing charts & features)
-- ===================================================================

-- ⚠️ IMPORTANT: Replace @USER_ID with your actual user_id
-- To get your user_id: SELECT user_id FROM users WHERE email = 'your@email.com';
-- Or use: SET @USER_ID = 1; (then run inserts below)

-- Quick setup: Uncomment and set your user_id
-- SET @USER_ID = 1;

-- ===================================================================
-- 4.1. BODY MEASUREMENTS - 7 days weight data (giảm từ 70kg → 68.5kg)
-- ===================================================================

-- Uncomment sau khi set @USER_ID
/*
INSERT INTO body_measurements (user_id, weight_kg, height_cm, date_recorded) VALUES
(@USER_ID, 70.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(@USER_ID, 69.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(@USER_ID, 69.5, 170.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(@USER_ID, 69.3, 170.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(@USER_ID, 69.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(@USER_ID, 68.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(@USER_ID, 68.5, 170.0, CURDATE());
*/

-- ===================================================================
-- 4.2. FOOD LOGS - 7 days eating data
-- ===================================================================

-- Day 6 ago (Breakfast: Phở bò, Lunch: Cơm trắng + Gà luộc, Dinner: Cơm tấm)
/*
INSERT INTO user_food_logs (user_id, food_id, date_eaten, meal_type, quantity) VALUES
(@USER_ID, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 'BREAKFAST', 1.0),  -- Phở bò 350
(@USER_ID, 11, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),    -- Cơm trắng 200
(@USER_ID, 18, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),    -- Gà luộc 165
(@USER_ID, 5, DATE_SUB(NOW(), INTERVAL 6 DAY), 'DINNER', 1.0),    -- Cơm tấm 500
-- Total: ~1215 kcal

-- Day 5 ago (Breakfast: Bánh mì, Lunch: Bún, Dinner: Phở gà)
(@USER_ID, 7, DATE_SUB(NOW(), INTERVAL 5 DAY), 'BREAKFAST', 1.0),  -- Bánh mì 350
(@USER_ID, 12, DATE_SUB(NOW(), INTERVAL 5 DAY), 'LUNCH', 1.0),     -- Bún 150
(@USER_ID, 16, DATE_SUB(NOW(), INTERVAL 5 DAY), 'LUNCH', 1.0),     -- Thịt bò 200
(@USER_ID, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 'DINNER', 1.0),     -- Phở gà 300
(@USER_ID, 32, DATE_SUB(NOW(), INTERVAL 5 DAY), 'SNACK', 1.0),     -- Chuối 105
-- Total: ~1105 kcal

-- Day 4 ago (Lower calories day)
(@USER_ID, 2, DATE_SUB(NOW(), INTERVAL 4 DAY), 'BREAKFAST', 1.0),  -- Phở gà 300
(@USER_ID, 23, DATE_SUB(NOW(), INTERVAL 4 DAY), 'LUNCH', 1.0),     -- Salad 80
(@USER_ID, 18, DATE_SUB(NOW(), INTERVAL 4 DAY), 'LUNCH', 1.0),     -- Gà luộc 165
(@USER_ID, 11, DATE_SUB(NOW(), INTERVAL 4 DAY), 'DINNER', 1.0),    -- Cơm 200
(@USER_ID, 21, DATE_SUB(NOW(), INTERVAL 4 DAY), 'DINNER', 1.0),    -- Rau xào 50
(@USER_ID, 33, DATE_SUB(NOW(), INTERVAL 4 DAY), 'SNACK', 1.0),     -- Táo 95
-- Total: ~890 kcal

-- Day 3 ago (Medium calories)
(@USER_ID, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'BREAKFAST', 1.0),  -- Phở bò 350
(@USER_ID, 4, DATE_SUB(NOW(), INTERVAL 3 DAY), 'LUNCH', 1.0),      -- Bún chả 450
(@USER_ID, 11, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DINNER', 1.0),    -- Cơm 200
(@USER_ID, 17, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DINNER', 1.0),    -- Cá kho 180
(@USER_ID, 31, DATE_SUB(NOW(), INTERVAL 3 DAY), 'SNACK', 1.0),     -- Yaourt 100
-- Total: ~1280 kcal

-- Day 2 ago (Higher calories)
(@USER_ID, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), 'BREAKFAST', 1.0),  -- Bánh mì 350
(@USER_ID, 25, DATE_SUB(NOW(), INTERVAL 2 DAY), 'BREAKFAST', 1.0), -- Cà phê sữa 150
(@USER_ID, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 'LUNCH', 1.0),      -- Cơm tấm 500
(@USER_ID, 11, DATE_SUB(NOW(), INTERVAL 2 DAY), 'DINNER', 1.0),    -- Cơm 200
(@USER_ID, 15, DATE_SUB(NOW(), INTERVAL 2 DAY), 'DINNER', 1.0),    -- Thịt lợn 250
-- Total: ~1450 kcal

-- Day 1 ago (Medium calories)
(@USER_ID, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 'BREAKFAST', 1.0),  -- Phở gà 300
(@USER_ID, 6, DATE_SUB(NOW(), INTERVAL 1 DAY), 'LUNCH', 1.0),      -- Cơm gà 450
(@USER_ID, 22, DATE_SUB(NOW(), INTERVAL 1 DAY), 'DINNER', 1.0),    -- Canh rau 30
(@USER_ID, 11, DATE_SUB(NOW(), INTERVAL 1 DAY), 'DINNER', 1.0),    -- Cơm 200
(@USER_ID, 34, DATE_SUB(NOW(), INTERVAL 1 DAY), 'SNACK', 1.0),     -- Cam 62
-- Total: ~1042 kcal

-- Today (Current day)
(@USER_ID, 1, CURDATE(), 'BREAKFAST', 1.0),                         -- Phở bò 350
(@USER_ID, 27, CURDATE(), 'BREAKFAST', 1.0),                        -- Nước cam 110
(@USER_ID, 11, CURDATE(), 'LUNCH', 1.0),                            -- Cơm 200
(@USER_ID, 18, CURDATE(), 'LUNCH', 1.0),                            -- Gà luộc 165
(@USER_ID, 21, CURDATE(), 'LUNCH', 1.0);                            -- Rau xào 50
-- Total so far: ~875 kcal
*/

-- ===================================================================
-- 4.3. EXERCISE LOGS - 7 days workout data
-- ===================================================================

/*
INSERT INTO user_exercise_logs (user_id, exercise_id, date_exercised, duration_minutes) VALUES
-- Day 6 ago: Chạy bộ 30 phút (300 kcal burned)
(@USER_ID, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 30.0),

-- Day 5 ago: Đạp xe 45 phút (375 kcal burned)
(@USER_ID, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), 45.0),

-- Day 4 ago: Yoga 60 phút (180 kcal burned)
(@USER_ID, 18, DATE_SUB(NOW(), INTERVAL 4 DAY), 60.0),

-- Day 3 ago: Chạy bộ + Push-up
(@USER_ID, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 25.0),
(@USER_ID, 12, DATE_SUB(NOW(), INTERVAL 3 DAY), 15.0),

-- Day 2 ago: Nhảy dây (high intensity)
(@USER_ID, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 20.0),

-- Day 1 ago: Đi bộ nhanh
(@USER_ID, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 40.0),

-- Today: Cầu lông
(@USER_ID, 15, CURDATE(), 30.0);
*/

-- ===================================================================
-- QUICK COPY-PASTE VERSION (Replace USER_ID with your actual ID)
-- ===================================================================

-- Just replace all "1" with your user_id and run:

/*
-- MEASUREMENTS
INSERT INTO body_measurements (user_id, weight_kg, height_cm, date_recorded) VALUES
(1, 70.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, 69.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(1, 69.5, 170.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(1, 69.3, 170.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(1, 69.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(1, 68.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(1, 68.5, 170.0, CURDATE());

-- FOOD LOGS (20 entries over 7 days)
INSERT INTO user_food_logs (user_id, food_id, date_eaten, meal_type, quantity) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 'BREAKFAST', 1.0),
(1, 11, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),
(1, 18, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),
(1, 5, DATE_SUB(NOW(), INTERVAL 6 DAY), 'DINNER', 1.0),
(1, 7, DATE_SUB(NOW(), INTERVAL 5 DAY), 'BREAKFAST', 1.0),
(1, 12, DATE_SUB(NOW(), INTERVAL 5 DAY), 'LUNCH', 1.0),
(1, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 'DINNER', 1.0),
(1, 2, DATE_SUB(NOW(), INTERVAL 4 DAY), 'BREAKFAST', 1.0),
(1, 23, DATE_SUB(NOW(), INTERVAL 4 DAY), 'LUNCH', 1.0),
(1, 18, DATE_SUB(NOW(), INTERVAL 4 DAY), 'LUNCH', 1.0),
(1, 11, DATE_SUB(NOW(), INTERVAL 4 DAY), 'DINNER', 1.0),
(1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 'BREAKFAST', 1.0),
(1, 4, DATE_SUB(NOW(), INTERVAL 3 DAY), 'LUNCH', 1.0),
(1, 11, DATE_SUB(NOW(), INTERVAL 3 DAY), 'DINNER', 1.0),
(1, 7, DATE_SUB(NOW(), INTERVAL 2 DAY), 'BREAKFAST', 1.0),
(1, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 'LUNCH', 1.0),
(1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 'BREAKFAST', 1.0),
(1, 6, DATE_SUB(NOW(), INTERVAL 1 DAY), 'LUNCH', 1.0),
(1, 1, CURDATE(), 'BREAKFAST', 1.0),
(1, 11, CURDATE(), 'LUNCH', 1.0);

-- EXERCISE LOGS (8 entries over 7 days)
INSERT INTO user_exercise_logs (user_id, exercise_id, date_exercised, duration_minutes) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 30.0),
(1, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), 45.0),
(1, 18, DATE_SUB(NOW(), INTERVAL 4 DAY), 60.0),
(1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 25.0),
(1, 12, DATE_SUB(NOW(), INTERVAL 3 DAY), 15.0),
(1, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 20.0),
(1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 40.0),
(1, 15, CURDATE(), 30.0);
*/

-- ===================================================================
-- VERIFICATION QUERIES
-- ===================================================================

-- Check inserted data:
-- SELECT COUNT(*) as food_count FROM foods;
-- SELECT COUNT(*) as exercise_count FROM exercises;
-- SELECT COUNT(*) as rule_count FROM chatbot_rules;

-- View sample foods:
-- SELECT food_id, food_name, calories_per_unit, serving_unit FROM foods LIMIT 10;

-- View sample exercises:
-- SELECT exercise_id, exercise_name, calories_burned_per_hour FROM exercises LIMIT 10;

-- ===================================================================
-- END OF SAMPLE DATA
-- ===================================================================

