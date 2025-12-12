# 📦 Sample Data Import Guide

## Tổng quan
File `sample_data.sql` chứa dữ liệu mẫu để test và demo ứng dụng BMI Chatbot.

## Nội dung dữ liệu

### 1. **Foods (38 items)**
Món ăn phổ biến Việt Nam:
- 🍜 Món chính: Phở, Bún bò, Cơm tấm, Bánh mì...
- 🍚 Cơm/Bún: Cơm trắng, Bún, Mì...
- 🥩 Thịt/Cá: Thịt lợn, bò, gà, cá, trứng...
- 🥗 Rau củ: Rau xào, salad, canh...
- ☕ Đồ uống: Cà phê, trà, nước cam, sữa...
- 🍰 Snack: Bánh ngọt, yaourt, trái cây...
- 🍔 Fast food: Gà rán, burger, pizza...

### 2. **Exercises (24 items)**
Các bài tập phổ biến:
- 🏃 Cardio: Chạy bộ, đi bộ, đạp xe, bơi lội...
- 💪 Gym: Tập tạ, squat, push-up, plank...
- ⚽ Thể thao: Đá bóng, cầu lông, tennis...
- 🧘 Yoga/Nhẹ: Yoga, aerobic, zumba...

### 3. **Chatbot Rules (8 rules)**
Câu trả lời tự động cho:
- 👋 Chào hỏi
- 📊 Thông tin BMI
- 📉 Cách giảm cân
- 📈 Cách tăng cân
- 🍽️ Gợi ý món ăn
- 💪 Gợi ý bài tập
- 🔥 Động lực/Motivation
- 📏 Thông tin Calories

---

## 🚀 Cách Import

### **Option 1: MySQL Workbench / phpMyAdmin**

1. Mở MySQL Workbench hoặc phpMyAdmin
2. Chọn database `bmi_chatbot`
3. Import file: `be/hienpc/src/main/resources/db/sample_data.sql`
4. Click **Execute** / **Go**

### **Option 2: Command Line**

```bash
# Vào thư mục chứa file SQL
cd be/hienpc/src/main/resources/db/

# Import vào database
mysql -u root -p bmi_chatbot < sample_data.sql

# Hoặc nếu có password
mysql -u root -pYOUR_PASSWORD bmi_chatbot < sample_data.sql
```

### **Option 3: DBeaver / Any SQL Client**

1. Connect to your MySQL database
2. Open `sample_data.sql` file
3. Execute the script
4. Verify data

### **⚠️ Important Notes:**

- SQL script đã được fix để match với schema entities
- Không có columns `created_at` và `created_by` trong `foods`/`exercises`
- `chatbot_rules` dùng `created_by_user_id` thay vì `created_by`
- Chỉ import 1 lần để tránh duplicate data

### **Option 4: Postman (Manual Testing)**

Import từng item qua API:

```
POST http://localhost:8080/admin/foods
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json

{
  "foodName": "Phở bò",
  "servingUnit": "tô",
  "caloriesPerUnit": 350
}
```

---

## 🧪 Verify Data

Sau khi import, kiểm tra:

```sql
-- Check food count
SELECT COUNT(*) as food_count FROM foods;
-- Expected: 38

-- Check exercise count  
SELECT COUNT(*) as exercise_count FROM exercises;
-- Expected: 24

-- Check chatbot rules
SELECT COUNT(*) as rule_count FROM chatbot_rules;
-- Expected: 8

-- View sample foods
SELECT food_id, food_name, calories_per_unit, serving_unit 
FROM foods 
LIMIT 10;

-- View sample exercises
SELECT exercise_id, exercise_name, calories_burned_per_hour 
FROM exercises 
LIMIT 10;

-- View chatbot rules
SELECT intent, keywords 
FROM chatbot_rules 
ORDER BY priority DESC;
```

---

## 📱 Test trên Android App

### 1. **Food Log Screen**
- Mở app → Log → Food Log
- Xem danh sách 38 món ăn
- Chọn "Phở bò" → Quantity: 1 → Save
- Kiểm tra Dashboard → Calories tăng 350 kcal

### 2. **Exercise Log Screen**
- Mở app → Log → Exercise Log
- Xem danh sách 24 bài tập
- Chọn "Chạy bộ" → Duration: 30 min → Save
- Kiểm tra Dashboard → Calories burned

### 3. **Chatbot**
- Mở app → Chat
- Gửi: "Chào bạn" → Nhận greeting response
- Gửi: "BMI là gì" → Nhận thông tin BMI
- Gửi: "Làm sao để giảm cân" → Nhận hướng dẫn
- Gửi: "Nên ăn gì" → Nhận gợi ý món ăn

### 4. **Dashboard Charts** - Import User Sample Data 📊

**File `sample_data.sql` đã có sẵn user sample data!**

#### **Bước 1: Lấy User ID của bạn**
```sql
SELECT user_id, email FROM users WHERE email = 'your@email.com';
-- Ví dụ kết quả: user_id = 1
```

#### **Bước 2: Copy-Paste & Replace User ID**

Mở file `sample_data.sql`, kéo xuống section **"QUICK COPY-PASTE VERSION"** (dòng ~170), uncomment và thay `1` bằng user_id của bạn:

```sql
-- MEASUREMENTS (7 days: 70kg → 68.5kg)
INSERT INTO body_measurements (user_id, weight_kg, height_cm, date_recorded) VALUES
(1, 70.0, 170.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(1, 69.8, 170.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
-- ... 7 entries total

-- FOOD LOGS (20 entries over 7 days)
INSERT INTO user_food_logs (user_id, food_id, date_eaten, meal_type, quantity) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 'BREAKFAST', 1.0),
(1, 11, DATE_SUB(NOW(), INTERVAL 6 DAY), 'LUNCH', 1.0),
-- ... 20 entries total

-- EXERCISE LOGS (8 entries over 7 days)
INSERT INTO user_exercise_logs (user_id, exercise_id, date_exercised, duration_minutes) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 6 DAY), 30.0),
(1, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), 45.0),
-- ... 8 entries total
```

#### **Bước 3: Execute SQL**

Run các INSERT statements đã uncomment.

#### **Bước 4: Verify Data**

```sql
-- Check measurements (expect: 7)
SELECT COUNT(*) FROM body_measurements WHERE user_id = 1;

-- Check food logs (expect: 20)
SELECT COUNT(*) FROM user_food_logs WHERE user_id = 1;

-- Check exercise logs (expect: 8)
SELECT COUNT(*) FROM user_exercise_logs WHERE user_id = 1;

-- View weight trend
SELECT date_recorded, weight_kg 
FROM body_measurements 
WHERE user_id = 1 
ORDER BY date_recorded;
```

#### **Bước 5: Test Charts trên App**

1. Mở app → Login với email đã dùng
2. Vào Dashboard
3. Pull to refresh
4. ✅ **Xem Weight Chart**: Đường giảm từ 70kg → 68.5kg
5. ✅ **Xem Calories Chart**: Bars với calories mỗi ngày
6. ✅ **Xem Insights**: "📉 Bạn đang giảm cân ổn định..."

#### **📊 Sample Data Summary:**

| Metric | Value |
|--------|-------|
| **Weight Start** | 70.0 kg |
| **Weight End** | 68.5 kg |
| **Weight Change** | -1.5 kg in 7 days |
| **Avg Calories/day** | ~1,140 kcal |
| **Total Food Logs** | 20 entries |
| **Total Exercise Logs** | 8 workouts |
| **Trend** | 📉 LOSING weight |

Sau khi insert, reload Dashboard → Xem Weight Chart & Calories Chart đẹp! 🎉

---

## 🗑️ Clear Data (Optional)

Nếu cần xóa để import lại:

```sql
-- CAREFUL: This will delete all data!
-- Delete in correct order to respect foreign keys
DELETE FROM user_exercise_logs;
DELETE FROM user_food_logs;
DELETE FROM body_measurements;
DELETE FROM chat_messages;
DELETE FROM chat_conversations;
DELETE FROM chatbot_rules WHERE created_by_user_id IS NULL;
DELETE FROM exercises;
DELETE FROM foods;
```

---

## 📝 Customize Data

Bạn có thể thêm món ăn/bài tập riêng:

### Thêm món ăn:
```sql
INSERT INTO foods (food_name, serving_unit, calories_per_unit) 
VALUES ('Bánh xèo', 'cái', 280);
```

### Thêm bài tập:
```sql
INSERT INTO exercises (exercise_name, calories_burned_per_hour) 
VALUES ('Boxing', 650);
```

### Thêm chatbot rule:
```sql
INSERT INTO chatbot_rules (intent, keywords, response_template, priority, created_by_user_id) 
VALUES ('water_info', 'nước,uống nước,bao nhiêu nước', 'Nên uống 2-3 lít nước mỗi ngày để cơ thể khỏe mạnh! 💧', 5, NULL);
```

---

## 🎯 Tips

1. **Import ngay từ đầu** để có data test
2. **Không import nhiều lần** - sẽ bị duplicate
3. **Customize** món ăn theo sở thích vùng miền
4. **Add more rules** cho chatbot thông minh hơn
5. **Create test users** để demo đầy đủ features

---

## 🐛 Troubleshooting

### Lỗi "Duplicate entry"
→ Data đã được import rồi. Chạy DELETE queries trước khi import lại.

### Lỗi "Table doesn't exist"
→ Chưa chạy migration. Run application lần đầu để Hibernate tạo tables.

### Chatbot không trả lời
→ Kiểm tra chatbot_rules đã import chưa:
```sql
SELECT * FROM chatbot_rules;
```

### Charts không hiển thị
→ Cần thêm user logs data (measurements + food/exercise logs).

---

## ✅ Checklist

- [ ] Import sample_data.sql thành công
- [ ] Verify: 38 foods, 24 exercises, 8 rules
- [ ] Test Food Log trên app
- [ ] Test Exercise Log trên app
- [ ] Test Chatbot với sample rules
- [ ] Add user measurement data
- [ ] Add user log data  
- [ ] Test Dashboard Charts
- [ ] Customize thêm data nếu cần

---

**Happy Testing!** 🚀

Last updated: 2025-12-12

