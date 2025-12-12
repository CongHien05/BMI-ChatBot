# Admin Portal User Guide - Hướng Dẫn Sử Dụng

## 📋 Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Đăng Nhập](#đăng-nhập)
3. [Dashboard](#dashboard)
4. [Quản Lý Món Ăn](#quản-lý-món-ăn)
5. [Quản Lý Bài Tập](#quản-lý-bài-tập)
6. [Quản Lý Chatbot Rules](#quản-lý-chatbot-rules)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Tổng Quan

Admin Portal là trang web quản lý dành cho admin, cho phép:
- ✅ Quản lý món ăn (Foods)
- ✅ Quản lý bài tập (Exercises)
- ✅ Quản lý chatbot rules
- ✅ Xem audit logs (sắp có)

**URL**: `http://localhost:8080/admin`

**Yêu Cầu**: Tài khoản có role `ADMIN`

---

## 🔐 Đăng Nhập

### Bước 1: Truy Cập Trang Login
- Mở browser, truy cập: `http://localhost:8080/admin/login`

### Bước 2: Nhập Thông Tin
- **Email**: Email của tài khoản admin
- **Password**: Mật khẩu của tài khoản admin

### Bước 3: Đăng Nhập
- Click nút **"🚀 Đăng nhập"**
- Nếu thành công, sẽ tự động chuyển đến Dashboard

### Lưu Ý:
- Token được lưu trong cookie (24 giờ)
- Nếu đã đăng nhập, truy cập `/admin/login` sẽ tự động redirect về Dashboard
- Để đăng xuất, click nút **"🚪 Đăng xuất"** ở header

---

## 🏠 Dashboard

### Truy Cập:
- URL: `http://localhost:8080/admin`
- Hoặc click logo/header từ bất kỳ trang nào

### Chức Năng:
- **Navigation Cards**: Click vào các card để điều hướng:
  - 📝 **Quản lý Món ăn**: Quản lý danh sách foods
  - 💪 **Quản lý Bài tập**: Quản lý danh sách exercises
  - 🤖 **Quản lý Chatbot Rules**: Quản lý chatbot rules

### Header:
- Hiển thị email admin đang đăng nhập
- Nút **"🚪 Đăng xuất"** để logout

---

## 📝 Quản Lý Món Ăn

### Truy Cập:
- URL: `http://localhost:8080/admin/foods`
- Hoặc click card **"📝 Quản lý Món ăn"** từ Dashboard

### Thêm Món Ăn Mới:

1. **Điền Form**:
   - **Tên món ăn**: VD: "Cơm trắng", "Thịt gà nướng"
   - **Đơn vị**: VD: "bát", "chén", "100g", "phần"
   - **Calories/Đơn vị**: Số calories cho mỗi đơn vị (VD: 130)

2. **Click "➕ Thêm Món ăn"**

3. **Kết Quả**:
   - Nếu thành công: Hiển thị message xanh "Món ăn đã được thêm thành công!"
   - Món ăn mới sẽ xuất hiện trong bảng danh sách

### Xem Danh Sách:
- Bảng hiển thị tất cả món ăn với:
  - **ID**: Mã số món ăn
  - **Tên món ăn**: Tên của món
  - **Đơn vị**: Đơn vị tính
  - **Calories/Đơn vị**: Số calories

### Lưu Ý:
- Tất cả fields đều bắt buộc
- Calories phải là số nguyên dương
- Mỗi thao tác thêm/sửa/xóa đều được ghi vào Audit Log

---

## 💪 Quản Lý Bài Tập

### Truy Cập:
- URL: `http://localhost:8080/admin/exercises`
- Hoặc click card **"💪 Quản lý Bài tập"** từ Dashboard

### Thêm Bài Tập Mới:

1. **Điền Form**:
   - **Tên bài tập**: VD: "Chạy bộ", "Bơi lội", "Đạp xe"
   - **Calories đốt cháy/Giờ**: Số calories đốt cháy trong 1 giờ (VD: 500)

2. **Click "➕ Thêm Bài tập"**

3. **Kết Quả**:
   - Nếu thành công: Hiển thị message xanh "Bài tập đã được thêm thành công!"
   - Bài tập mới sẽ xuất hiện trong bảng danh sách

### Xem Danh Sách:
- Bảng hiển thị tất cả bài tập với:
  - **ID**: Mã số bài tập
  - **Tên bài tập**: Tên của bài tập
  - **Calories/Giờ**: Số calories đốt cháy mỗi giờ

### Lưu Ý:
- Tất cả fields đều bắt buộc
- Calories phải là số nguyên dương
- Mỗi thao tác thêm/sửa/xóa đều được ghi vào Audit Log

---

## 🤖 Quản Lý Chatbot Rules

### Truy Cập:
- URL: `http://localhost:8080/admin/rules`
- Hoặc click card **"🤖 Quản lý Chatbot Rules"** từ Dashboard

### Thêm Rule Mới:

1. **Điền Form**:
   - **Intent**: Phân loại rule (VD: `FAQ`, `ADD_WEIGHT`, `GREETING`)
   - **Keywords**: Từ khóa để match (phân cách bằng dấu phẩy)
     - VD: `tăng cân, gain weight, muốn tăng cân`
   - **Response Template**: Câu trả lời của chatbot
   - **Priority**: Độ ưu tiên (mặc định = 0, số cao hơn = ưu tiên hơn)

2. **Click "➕ Thêm Rule"**

3. **Kết Quả**:
   - Nếu thành công: Hiển thị message xanh "Rule đã được thêm thành công!"
   - Rule mới sẽ xuất hiện trong bảng danh sách

### Xem Danh Sách:
- Bảng hiển thị tất cả rules với:
  - **ID**: Mã số rule
  - **Intent**: Loại intent
  - **Keywords**: Danh sách keywords
  - **Response Template**: Template câu trả lời
  - **Priority**: Độ ưu tiên

### Lưu Ý:
- Xem chi tiết hướng dẫn tạo rules tại: `CHATBOT_TRAINING_MANUAL.md`
- Mỗi thao tác thêm/sửa/xóa đều được ghi vào Audit Log

---

## 🔍 Navigation

### Menu Điều Hướng:
Ở mỗi trang quản lý, có menu navigation ở đầu trang:
- **📝 Quản lý Món ăn**: Chuyển đến `/admin/foods`
- **💪 Quản lý Bài tập**: Chuyển đến `/admin/exercises`
- **🤖 Quản lý Rules**: Chuyển đến `/admin/rules`

### Header:
- **Email admin**: Hiển thị email đang đăng nhập
- **Nút Đăng xuất**: Logout và quay về trang login

---

## ⚠️ Troubleshooting

### Vấn Đề 1: Không Đăng Nhập Được

**Nguyên nhân có thể:**
- Email/password sai
- Tài khoản không có role `ADMIN`
- Server chưa chạy

**Giải pháp:**
1. Kiểm tra email/password
2. Kiểm tra trong database: `SELECT * FROM users WHERE email = 'your@email.com'`
   - Đảm bảo `role = 'ADMIN'`
3. Kiểm tra server đang chạy tại `http://localhost:8080`

---

### Vấn Đề 2: Không Thấy CSS/JS

**Nguyên nhân:**
- Static resources bị chặn bởi Spring Security

**Giải pháp:**
1. Kiểm tra `SecurityConfig` đã permit `/css/**`, `/js/**`
2. Kiểm tra file CSS/JS có trong `src/main/resources/static/`
3. Clear browser cache và reload

---

### Vấn Đề 3: Form Submit Không Hoạt Động

**Nguyên nhân:**
- Token hết hạn
- JavaScript error

**Giải pháp:**
1. Kiểm tra console browser (F12) xem có lỗi JavaScript không
2. Đăng nhập lại để lấy token mới
3. Kiểm tra network tab xem request có được gửi không

---

### Vấn Đề 4: Không Thấy Thông Báo Success/Error

**Nguyên nhân:**
- Flash attributes không được truyền đúng

**Giải pháp:**
1. Kiểm tra controller có dùng `RedirectAttributes` không
2. Kiểm tra template có hiển thị `successMessage`/`errorMessage` không
3. Reload trang để xem message

---

## 🔒 Security

### Authentication:
- Admin portal sử dụng **JWT authentication**
- Token được lưu trong **cookie** (24 giờ)
- Mỗi request đến `/admin/**` đều được kiểm tra token

### Authorization:
- Chỉ user có `role = 'ADMIN'` mới truy cập được
- Nếu không có quyền, sẽ bị redirect về login hoặc 403 Forbidden

### Audit Log:
- Mọi thao tác của admin đều được ghi vào `audit_logs` table
- Bao gồm: action, entity, entity_id, details, timestamp, user

---

## 📊 Best Practices

### 1. **Quản Lý Foods/Exercises:**
- ✅ Thêm đầy đủ thông tin (tên, đơn vị, calories)
- ✅ Kiểm tra dữ liệu trước khi thêm (tránh trùng lặp)
- ✅ Sử dụng đơn vị chuẩn (100g, bát, chén)

### 2. **Quản Lý Rules:**
- ✅ Xem `CHATBOT_TRAINING_MANUAL.md` để biết cách tạo rules tốt
- ✅ Test rules sau khi tạo (gửi message tương tự)
- ✅ Điều chỉnh priority nếu cần

### 3. **Security:**
- ✅ Đăng xuất khi không dùng
- ✅ Không chia sẻ tài khoản admin
- ✅ Đổi password định kỳ

---

## 🚀 Tính Năng Sắp Có

### Đang Phát Triển:
- ✏️ **Edit/Delete**: Sửa và xóa foods/exercises/rules
- 📊 **Dashboard Stats**: Thống kê tổng quan hệ thống
- 📋 **Audit Logs UI**: Xem danh sách audit logs
- 👥 **User Management**: Quản lý users

---

**Ngày tạo:** 2025-11-26  
**Phiên bản:** 1.0  
**Tác giả:** Cursor AI Assistant

