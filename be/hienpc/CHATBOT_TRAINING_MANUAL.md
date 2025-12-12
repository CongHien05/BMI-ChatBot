# Chatbot Training Manual - Hướng Dẫn Tạo Rules

## 📋 Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Cách Chatbot Hoạt Động](#cách-chatbot-hoạt-động)
3. [Cách Tạo Rule](#cách-tạo-rule)
4. [Best Practices](#best-practices)
5. [Ví Dụ Thực Tế](#ví-dụ-thực-tế)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 Tổng Quan

Chatbot BMI sử dụng **hybrid approach** (kết hợp rule-based và AI):
- **Rule-based**: Kiểm tra keywords trong message → trả về response template
- **AI Fallback (Gemini)**: Nếu không có rule nào match → gọi Gemini API

### Ưu Điểm:
- ✅ Rule-based: Nhanh, chính xác, có thể kiểm soát
- ✅ AI Fallback: Xử lý được câu hỏi phức tạp, không có trong rules

---

## 🔄 Cách Chatbot Hoạt Động

### Flow Xử Lý Message:

```
User gửi message
    ↓
1. Normalize message (lowercase, trim)
    ↓
2. Lấy tất cả rules, sắp xếp theo Priority (cao → thấp)
    ↓
3. Kiểm tra từng rule:
   - Tách keywords (phân cách bằng dấu phẩy)
   - Kiểm tra xem message có chứa keyword nào không
    ↓
4. Nếu có rule match:
   → Trả về response_template của rule đó
    ↓
5. Nếu không có rule nào match:
   → Gọi Gemini API (fallback)
   → Trả về response từ Gemini
```

### Priority System:
- **Priority cao hơn** = được kiểm tra trước
- Nếu nhiều rules match, rule có priority cao nhất sẽ được chọn
- **Ví dụ**: Rule "ADD_WEIGHT" (priority=10) sẽ được chọn trước rule "FAQ" (priority=5)

---

## ✍️ Cách Tạo Rule

### Bước 1: Truy Cập Admin Portal
1. Đăng nhập tại: `http://localhost:8080/admin/login`
2. Vào trang **"🤖 Quản lý Chatbot Rules"**

### Bước 2: Điền Form

#### **Intent** (Bắt buộc)
- **Mục đích**: Phân loại rule, giúp quản lý dễ dàng
- **Ví dụ**: `FAQ`, `ADD_WEIGHT`, `NUTRITION_TIPS`, `EXERCISE_ADVICE`, `GREETING`
- **Lưu ý**: Nên dùng UPPERCASE với underscore (convention)

#### **Keywords** (Bắt buộc)
- **Mục đích**: Từ khóa để match với user message
- **Format**: Phân cách bằng dấu phẩy (`,`)
- **Ví dụ**: `tăng cân, gain weight, muốn tăng cân, tăng trọng lượng`
- **Lưu ý**:
  - Nên thêm nhiều biến thể (tiếng Việt + tiếng Anh)
  - Có thể thêm từ đồng nghĩa
  - Không phân biệt hoa thường (tự động lowercase)

#### **Response Template** (Bắt buộc)
- **Mục đích**: Câu trả lời của chatbot
- **Ví dụ**: 
  ```
  Bạn muốn tăng cân hả? Để tăng cân hiệu quả, bạn cần:
  1. Ăn nhiều calo hơn mức tiêu thụ
  2. Tập luyện để tăng cơ
  3. Ngủ đủ giấc (7-8 tiếng/ngày)
  ```
- **Lưu ý**:
  - Có thể dùng xuống dòng (`\n`) hoặc HTML
  - Nên viết ngắn gọn, dễ hiểu
  - Có thể thêm emoji để thân thiện hơn

#### **Priority** (Tùy chọn, mặc định = 0)
- **Mục đích**: Độ ưu tiên khi kiểm tra rules
- **Range**: 0-100 (số càng cao = ưu tiên càng cao)
- **Ví dụ**: 
  - Rule cụ thể (ADD_WEIGHT) → Priority = 10
  - Rule chung (FAQ) → Priority = 5
  - Rule greeting → Priority = 1

### Bước 3: Lưu Rule
- Click **"➕ Thêm Rule"**
- Rule sẽ được lưu vào database và có hiệu lực ngay

---

## 💡 Best Practices

### 1. **Keywords Nên Có:**
✅ **Nhiều biến thể**:
```
tăng cân, gain weight, muốn tăng, tăng trọng lượng, muốn béo lên
```

✅ **Cả tiếng Việt và tiếng Anh**:
```
giảm cân, lose weight, giảm béo, weight loss
```

✅ **Từ đồng nghĩa**:
```
BMI, chỉ số BMI, body mass index, chỉ số khối cơ thể
```

### 2. **Keywords Không Nên:**
❌ **Quá ngắn hoặc quá chung**:
```
cân, weight, ăn, food
```
→ Sẽ match quá nhiều message không liên quan

❌ **Quá dài hoặc quá cụ thể**:
```
tôi muốn tăng cân nhưng không biết bắt đầu từ đâu và cần lời khuyên chi tiết
```
→ Khó match với user message

### 3. **Response Template Nên:**
✅ **Ngắn gọn, dễ hiểu** (2-5 câu)
✅ **Có cấu trúc** (số thứ tự, bullet points)
✅ **Thân thiện** (dùng "bạn", emoji)
✅ **Hữu ích** (cung cấp thông tin cụ thể)

### 4. **Priority Strategy:**
- **Rule cụ thể** (ADD_WEIGHT, LOSE_WEIGHT) → Priority cao (10-20)
- **Rule chung** (FAQ, GREETING) → Priority trung bình (5-10)
- **Rule fallback** (DEFAULT) → Priority thấp (0-5)

### 5. **Intent Naming:**
- Dùng UPPERCASE với underscore
- Ngắn gọn, mô tả rõ mục đích
- **Ví dụ**: `ADD_WEIGHT`, `NUTRITION_TIPS`, `EXERCISE_ADVICE`

---

## 📝 Ví Dụ Thực Tế

### Ví Dụ 1: Rule "Tăng Cân"

**Intent**: `ADD_WEIGHT`

**Keywords**: 
```
tăng cân, gain weight, muốn tăng cân, tăng trọng lượng, muốn béo lên, làm sao để tăng cân
```

**Response Template**:
```
Bạn muốn tăng cân hả? 💪 Để tăng cân hiệu quả và lành mạnh:

1. **Ăn nhiều calo hơn**: Tăng 300-500 calo/ngày so với mức hiện tại
2. **Tập luyện**: Tập tạ để tăng cơ, không chỉ tăng mỡ
3. **Ngủ đủ**: 7-8 tiếng/ngày để cơ thể phục hồi
4. **Ăn đủ protein**: 1.6-2.2g/kg thể trọng

Bạn có muốn tôi giúp tính toán calo cần thiết không?
```

**Priority**: `10`

---

### Ví Dụ 2: Rule "Chào Hỏi"

**Intent**: `GREETING`

**Keywords**: 
```
xin chào, hello, hi, chào bạn, chào, hey
```

**Response Template**:
```
Xin chào! 👋 Mình là chatbot BMI, sẵn sàng giúp bạn:
- Theo dõi cân nặng và BMI
- Tư vấn dinh dưỡng
- Gợi ý bài tập
- Trả lời câu hỏi về sức khỏe

Bạn cần hỗ trợ gì hôm nay?
```

**Priority**: `5`

---

### Ví Dụ 3: Rule "Giảm Cân"

**Intent**: `LOSE_WEIGHT`

**Keywords**: 
```
giảm cân, lose weight, giảm béo, weight loss, muốn giảm, làm sao để giảm cân
```

**Response Template**:
```
Bạn muốn giảm cân hả? 🏃 Để giảm cân an toàn:

1. **Tạo calo deficit**: Ăn ít hơn 300-500 calo/ngày so với mức tiêu thụ
2. **Tập cardio**: 150 phút/tuần (đi bộ, chạy, đạp xe)
3. **Tập tạ**: Giữ cơ trong quá trình giảm cân
4. **Ăn đủ protein**: Giảm cảm giác đói, giữ cơ

Bạn có muốn tôi giúp tính toán calo cần thiết không?
```

**Priority**: `10`

---

## 🔧 Troubleshooting

### Vấn Đề 1: Rule Không Match

**Nguyên nhân có thể:**
- Keywords không khớp với cách user viết
- Priority quá thấp, bị rule khác match trước
- User message quá ngắn hoặc quá dài

**Giải pháp:**
1. Thêm nhiều biến thể keywords hơn
2. Tăng priority của rule
3. Kiểm tra lại message của user (có thể test bằng cách gửi message tương tự)

---

### Vấn Đề 2: Nhiều Rules Match Cùng Lúc

**Nguyên nhân:**
- Keywords của các rules trùng nhau
- Priority bằng nhau

**Giải pháp:**
1. Làm keywords cụ thể hơn (tránh trùng)
2. Điều chỉnh priority (rule cụ thể hơn → priority cao hơn)

---

### Vấn Đề 3: Response Không Phù Hợp

**Nguyên nhân:**
- Response template quá chung chung
- Không đủ thông tin

**Giải pháp:**
1. Viết response cụ thể hơn
2. Thêm ví dụ, số liệu cụ thể
3. Có thể thêm link hoặc hướng dẫn chi tiết

---

## 📊 Monitoring & Improvement

### Cách Cải Thiện Rules:

1. **Theo dõi Chat History** (nếu có):
   - Xem messages nào không match rule nào
   - Tạo rule mới dựa trên messages đó

2. **Test Thường Xuyên**:
   - Gửi các message khác nhau để test rules
   - Điều chỉnh keywords và priority nếu cần

3. **Cập Nhật Response**:
   - Cập nhật response template khi có thông tin mới
   - Làm cho response thân thiện, hữu ích hơn

---

## 🎓 Tips Nâng Cao

### 1. **Sử Dụng Intent Để Xử Lý Đặc Biệt** (Future)
Hiện tại chatbot chỉ trả về `response_template`. Trong tương lai, có thể xử lý intent đặc biệt:
- `ADD_WEIGHT`: Parse message, lưu vào BodyMeasurement
- `CALCULATE_BMI`: Tính toán BMI từ weight/height
- `LOG_FOOD`: Gợi ý log food

### 2. **Template Variables** (Future)
Có thể thêm biến vào response template:
```
Xin chào {user_name}! Bạn đang có BMI là {bmi}.
```

### 3. **Context-Aware Responses** (Future)
Response có thể thay đổi dựa trên:
- User profile (goal, current weight)
- History (đã hỏi gì trước đó)
- Time of day

---

**Ngày tạo:** 2025-11-26  
**Phiên bản:** 1.0  
**Tác giả:** Cursor AI Assistant

