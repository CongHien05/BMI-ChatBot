# Đề Xuất Chức Năng Admin Portal - BMI Chatbot

## 📊 Tổng Quan

Admin portal hiện tại đã có các chức năng cơ bản. Dưới đây là đề xuất các chức năng bổ sung để hoàn thiện hệ thống quản lý.

---

## 🎯 Ưu Tiên Cao (Nên có ngay)

### 1. **Dashboard với Thống Kê Tổng Quan**
**Mục đích:** Admin cần xem tổng quan hệ thống để quản lý hiệu quả

**Chức năng:**
- 📈 Số lượng users (tổng, active, mới trong tháng)
- 📊 Số lượng foods/exercises/rules
- 📉 Biểu đồ tăng trưởng users theo thời gian
- 🔥 Top users tích cực nhất (log nhiều nhất)
- 📱 Số lượng chat messages hôm nay/tuần/tháng
- ⚠️ Cảnh báo: Users không hoạt động > 30 ngày

**API cần:**
- `GET /admin/api/stats/overview` - Tổng quan
- `GET /admin/api/stats/users` - Thống kê users
- `GET /admin/api/stats/activity` - Hoạt động

---

### 2. **Edit/Delete cho Foods, Exercises, Rules**
**Mục đích:** Hiện tại chỉ có thêm, cần sửa/xóa để quản lý đầy đủ

**Chức năng:**
- ✏️ Edit Food/Exercise/Rule (form tương tự add)
- 🗑️ Delete với confirmation dialog
- 🔍 Search/Filter trong danh sách
- 📄 Pagination cho danh sách dài

**Routes:**
- `GET /admin/foods/{id}/edit` - Form edit
- `POST /admin/foods/{id}/update` - Update
- `POST /admin/foods/{id}/delete` - Delete
- (Tương tự cho exercises và rules)

---

### 3. **Xem Audit Logs**
**Mục đích:** Theo dõi mọi thao tác của admin để audit và debug

**Chức năng:**
- 📋 Danh sách tất cả audit logs
- 🔍 Filter theo: action, entity, admin, date range
- 📊 Thống kê: Admin nào thao tác nhiều nhất
- 📥 Export logs ra CSV/Excel

**Routes:**
- `GET /admin/audit-logs` - Danh sách logs
- `GET /admin/audit-logs/export` - Export CSV

---

### 4. **Quản Lý Users**
**Mục đích:** Admin cần quản lý users, khóa tài khoản vi phạm, xem thông tin

**Chức năng:**
- 👥 Danh sách users với search/filter
- 👤 Xem chi tiết user: profile, body measurements, logs
- 🔒 Khóa/Mở khóa tài khoản (thêm field `isActive` vào User)
- 🗑️ Xóa user (soft delete hoặc hard delete)
- 📊 Thống kê: Users mới, active users, inactive users

**Routes:**
- `GET /admin/users` - Danh sách users
- `GET /admin/users/{id}` - Chi tiết user
- `POST /admin/users/{id}/toggle-active` - Khóa/Mở khóa
- `POST /admin/users/{id}/delete` - Xóa user

---

## 🎯 Ưu Tiên Trung Bình (Nên có sau)

### 5. **Analytics & Reports**
**Mục đích:** Phân tích dữ liệu để hiểu hành vi users và cải thiện dịch vụ

**Chức năng:**
- 📊 BMI Distribution Chart (bao nhiêu % users ở mức nào)
- 📈 Weight Loss/Gain Trends (theo thời gian)
- 🍎 Top Foods được log nhiều nhất
- 💪 Top Exercises được log nhiều nhất
- 📅 Activity Heatmap (users hoạt động theo ngày/tuần)
- 📥 Export reports ra PDF/Excel

**Routes:**
- `GET /admin/analytics/bmi-distribution`
- `GET /admin/analytics/weight-trends`
- `GET /admin/analytics/popular-foods`
- `GET /admin/analytics/popular-exercises`

---

### 6. **Chat History & Analytics**
**Mục đích:** Xem lịch sử chat để cải thiện chatbot rules

**Chức năng:**
- 💬 Xem chat history của users (anonymized hoặc với permission)
- 🔍 Search messages theo keyword
- 📊 Thống kê: Intent nào được hỏi nhiều nhất
- 🎯 Messages không match rule nào (để tạo rule mới)
- 📈 Chatbot performance: response time, satisfaction rate

**Cần thêm Entity:**
```java
@Entity
public class ChatHistory {
    private Integer chatId;
    private User user;
    private String userMessage;
    private String botResponse;
    private String matchedRuleId; // null nếu dùng Gemini
    private LocalDateTime createdAt;
}
```

**Routes:**
- `GET /admin/chat-history` - Danh sách chats
- `GET /admin/chat-analytics` - Thống kê chat

---

### 7. **Quản Lý User Profiles**
**Mục đích:** Xem và chỉnh sửa profile của users nếu cần

**Chức năng:**
- 👤 Xem profile của user (goal, target weight, etc.)
- ✏️ Edit profile (nếu user yêu cầu hỗ trợ)
- 📊 Xem body measurements history (biểu đồ)
- 📈 BMI trend chart cho user

**Routes:**
- `GET /admin/users/{id}/profile` - Xem profile
- `POST /admin/users/{id}/profile/update` - Update profile

---

### 8. **Bulk Operations**
**Mục đích:** Thao tác hàng loạt để tiết kiệm thời gian

**Chức năng:**
- 📦 Import Foods/Exercises từ CSV/Excel
- 📥 Export Foods/Exercises ra CSV/Excel
- 🗑️ Bulk delete (chọn nhiều items và xóa)
- 🔄 Bulk update (update nhiều items cùng lúc)

**Routes:**
- `POST /admin/foods/import` - Import từ file
- `GET /admin/foods/export` - Export ra file
- `POST /admin/foods/bulk-delete` - Xóa nhiều items

---

## 🎯 Ưu Tiên Thấp (Nice to Have)

### 9. **System Settings**
**Mục đích:** Cấu hình hệ thống

**Chức năng:**
- ⚙️ Cấu hình JWT expiration time
- 🔑 Cấu hình Gemini API key (nếu có)
- 📧 Email settings (SMTP config)
- 🔔 Notification settings
- 🌐 System maintenance mode (tạm dừng hệ thống)

**Routes:**
- `GET /admin/settings` - Xem settings
- `POST /admin/settings/update` - Update settings

---

### 10. **Role Management (Nếu có nhiều admin)**
**Mục đích:** Phân quyền chi tiết cho nhiều admin

**Chức năng:**
- 👥 Quản lý admin users
- 🔐 Phân quyền: Admin có thể quản lý foods nhưng không thể xóa users
- 📋 Role-based access control (RBAC)

**Cần thêm:**
- Entity `Role` và `Permission`
- Many-to-Many giữa User và Role

---

### 11. **Notifications & Alerts**
**Mục đích:** Cảnh báo admin về các sự kiện quan trọng

**Chức năng:**
- ⚠️ Alert khi có user report bug
- 📊 Alert khi số lượng users tăng đột biến
- 🔥 Alert khi chatbot không match rule nào nhiều lần
- 📧 Email notifications cho admin

---

### 12. **Backup & Restore**
**Mục đích:** Sao lưu và khôi phục dữ liệu

**Chức năng:**
- 💾 Backup database (manual hoặc scheduled)
- 🔄 Restore từ backup
- 📥 Download backup file
- 📅 Lịch sử backups

---

## 📋 Tóm Tắt Đề Xuất

### Nên làm ngay (Sprint 3 hoặc 4):
1. ✅ Dashboard với thống kê
2. ✅ Edit/Delete cho Foods, Exercises, Rules
3. ✅ Xem Audit Logs
4. ✅ Quản lý Users cơ bản

### Nên làm sau (Sprint 4+):
5. Analytics & Reports
6. Chat History & Analytics
7. Quản lý User Profiles
8. Bulk Operations

### Nice to have:
9. System Settings
10. Role Management
11. Notifications & Alerts
12. Backup & Restore

---

## 🎨 UI/UX Improvements

### Cải thiện giao diện hiện tại:
- ✅ Đã có: Modern CSS với gradients, animations
- 🔄 Nên thêm:
  - Loading spinners khi đang tải
  - Toast notifications (thay vì alert)
  - Confirmation dialogs trước khi xóa
  - Search bar với autocomplete
  - Data tables với sorting, pagination
  - Charts (Chart.js hoặc ApexCharts)

---

## 🔧 Technical Improvements

### Backend:
- Validation tốt hơn (custom validators)
- Pagination cho tất cả list endpoints
- Caching cho thống kê (Redis nếu cần)
- Background jobs cho reports (Spring Batch)

### Security:
- Rate limiting cho admin endpoints
- IP whitelist (optional)
- 2FA cho admin login (optional)

---

**Ngày tạo:** 2025-11-26  
**Tác giả:** Cursor AI Assistant

