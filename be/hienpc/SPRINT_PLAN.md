# Sprint Plan Overview - BMI Chatbot Project

## 📋 Tổng quan dự án
- **Tech stack Backend**: Spring Boot 3 (Java 17), Spring Security 6, JPA/Hibernate, MySQL, Thymeleaf (Admin Portal)
- **Tech stack Frontend**: Android (Kotlin, MVVM), Retrofit, Coroutines, LiveData, ViewModel, RecyclerView
- **Hạ tầng/CI**: GitHub Actions (maven test + android lint/unit test), Docker Compose (dev), Flyway migration từ Sprint 2
- **Môi trường**: Local dev, staging (Docker), production (cloud TBD)
- **Mục tiêu tổng thể**: Ứng dụng BMI giúp người dùng log dinh dưỡng/luyện tập, theo dõi dashboard, trò chuyện chatbot; Admin quản lý dữ liệu, “huấn luyện” chatbot.

---

## 🔄 Lộ trình chính (3 Sprint + backlog mở rộng)
1. **Sprint 1 – Backend Foundation & Auth** (1 – 1.5 tuần)
2. **Sprint 2 – Logging, Dashboard & Profile** (1.5 tuần)
3. **Sprint 3 – Chatbot Hybrid & Admin Portal** (1 – 1.5 tuần)
4. **Sprint 4+ (Backlog mở rộng)** – Notification, Analytics, Release Prep

> Mỗi sprint: backlog BE/FE, deliverables, tiêu chí nghiệm thu, yêu cầu test & tài liệu.

---

## Sprint 1 – Backend Foundation & Auth
> **Mục tiêu**: Nền tảng backend + auth API; frontend dựng skeleton, mock; chuẩn hóa tài liệu.

### Backend Tasks
- [x] Cấu hình MySQL, profile dev, chuẩn bị Flyway baseline
- [x] Entities/repositories: User, Admin, UserProfile, BodyMeasurement (quan hệ 1-1 & 1-n)
- [x] Spring Security 6 stateless + JWT
- [x] Service layer: `JwtService`, `UserService`, password hashing, validation
- [x] DTO + Controller: Auth (register/login)
- [x] Swagger/OpenAPI cho auth endpoints
- [x] Unit test (JwtService, UserService) với Mockito
- [x] Postman collection + docs (Architecture, JWT, Postman guide)

### Frontend Tasks (Android)
- [x] Khởi tạo project, Gradle deps (Retrofit, Gson, Coroutines, Lifecycle, DataStore)
- [x] Retrofit client (`ApiClient`, base URL 10.0.2.2)
- [x] Auth DTOs, API interface, ViewModel skeleton
- [x] UI mock login/register, flow điều hướng
- [ ] Local DataStore chuẩn bị lưu token (mock)
- [x] Unit tests ViewModel (fake repository)

### Deliverables Sprint 1
- Auth API chạy được, JWT hoạt động, Swagger/Postman cập nhật
- Android UI auth + ViewModel mock sẵn sàng gọi API thật
- Tài liệu: Architecture explanation, JWT flow, Postman testing guide

### Acceptance Criteria
- Đăng ký/đăng nhập + bảo vệ endpoint khác bằng JWT
- Password hash BCrypt, lỗi trả JSON chuẩn
- Android build thành công, UI hiển thị với mock data
- Swagger UI public `/swagger-ui.html`

---

## Sprint 2 – Logging, Dashboard & Profile
> **Mục tiêu**: API logging, dashboard, profile update; Android tích hợp API thật, lưu token, hiển thị dữ liệu.

### Backend Tasks
- [ ] Flyway migration: Food, Exercise, UserFoodLog, UserExerciseLog
- [x] Entities/repositories: Food, Exercise (Many-to-One Admin), UserFoodLog, UserExerciseLog
- [x] DTOs: `FoodResponse`, `ExerciseResponse`, `FoodLogRequest`, `ExerciseLogRequest`, `DashboardSummary`, `ProfileUpdateRequest`
- [x] API:
  - `GET /api/foods`, `GET /api/exercises`
  - `POST /api/logs/food`, `POST /api/logs/exercise` (JWT user, validation)
  - `GET /api/dashboard/summary` (current weight, BMI, calories today)
  - `PUT /api/profile` (update goals, target weight, gender, etc.)
- [x] Global exception handler + error schema thống nhất
- [x] Unit + integration tests (service/controller)
- [x] Swagger/Postman update + guide test logging/dashboard/profile

### Frontend Tasks
- [ ] Lưu JWT bằng DataStore, interceptor thêm Bearer token
- [ ] Retrofit service + data models cho Food/Exercise/Logs/Dashboard/Profile
- [ ] ViewModel/UI:
  - Food log screen (spinner/list, quantity input)
  - Exercise log screen
  - Dashboard fragment (weight, BMI, calories, mini chart)
  - Profile screen (update mục tiêu)
- [ ] Error/loading state (sealed class), retry logic
- [ ] Automated tests (unit + instrumentation)
- [ ] Demo video flow login → dashboard → log → profile update

### Deliverables Sprint 2
- Backend logging/dashboard/profile API fully tested
- Android gọi API thật, hiển thị dashboard, log food/exercise, update profile
- Docs: API usage, payload samples, QA checklist

### Acceptance Criteria
- User đăng nhập trên Android → log food/exercise → dashboard cập nhật đúng số liệu ngày hiện tại
- API lỗi trả đúng format cho FE xử lý
- Token lưu an toàn, refresh UI sau log
- Tests xanh (backend unit/integration + frontend unit/instrumentation)

---

## Sprint 3 – Chatbot Rule-Based & Admin Portal
> **Mục tiêu**: Hoàn thiện chatbot rule-based + fallback Gemini, thêm portal web để admin quản lý rule/food/exercise và audit thao tác.

### Backend Tasks
- [x] Entities/repositories: `ChatbotRule` (intent, keywords, priority, createdBy), `AuditLog`
- [x] Chatbot API & service cơ bản
  - [x] `ChatRequest` / `ChatResponse`
  - [x] `POST /api/chatbot` (Smart routing: rule-based trước, sau đó fallback Gemini)
  - [x] `ChatbotService`: match keywords theo `ChatbotRule`, ưu tiên theo `priority`, fallback `GeminiApiService`
  - [x] `GeminiApiService`: skeleton fallback (chưa gọi API thật)
- [x] Admin portal (Thymeleaf + Spring MVC)
  - [x] SecurityConfig bảo vệ `/admin/**` với role ADMIN (JWT-based)
  - [x] Trang `/admin` (dashboard) với navigation links
  - [x] Trang `/admin/rules`: list + form add ChatbotRule (`AdminChatbotRuleController`)
  - [x] Trang `/admin/foods`: list + form add Food (`AdminFoodController`)
  - [x] Trang `/admin/exercises`: list + form add Exercise (`AdminExerciseController`)
  - [x] Thymeleaf templates: `dashboard.html`, `manage-rules.html`, `manage-foods.html`, `manage-exercises.html`
  - [x] `AuditLogService` ghi log mỗi khi admin thêm rule/food/exercise
- [x] JWT filter cho tất cả API (trừ `/api/auth/**` + Swagger)
- [x] Unit + WebMvc tests cho chatbot flow (service + controller)
- [x] Docs: chatbot training manual (cách viết rule, keywords), admin portal user guide, Gemini key setup (mô tả cấu hình, chưa cần tích hợp thật)

### Frontend Tasks (Android)
- [ ] Chat UI hoàn chỉnh (RecyclerView 2 view type, auto scroll, timestamp)
- [ ] `ChatViewModel` lưu danh sách message, Room (local history)
- [ ] Tích hợp chatbot API: gửi message, hiển thị response, loading/error states
- [ ] UX nâng cao: animation gửi/nhận, pull-to-refresh history, offline notice
- [ ] Dashboard chart nâng cấp (weekly/monthly trends), filter log history
- [ ] Manual test checklist + video demo toàn app

### Deliverables Sprint 3
- Chatbot backend (rule + Gemini fallback) chạy ổn định
- Admin portal quản lý foods/exercises/rules, có audit
- Android chat UI hoạt động realtime, có lịch sử
- Tài liệu vận hành (admin, chatbot, release checklist)

### Acceptance Criteria
- Admin thêm/sửa/xóa rule/food/exercise trên portal (được bảo vệ)
- Chatbot phản hồi đúng rule, fallback Gemini khi không match
- Android chat hiển thị mượt, xử lý lỗi hợp lý
- Bộ docs hoàn chỉnh cho vận hành & release

---

## Sprint 4+ – Backlog mở rộng (đề xuất)
- Notification & Reminder (Firebase Cloud Messaging) nhắc log, uống nước
- Advanced analytics (macro nutrients, trend BMI, xuất PDF report)
- Multi-language (vi/en) cho app & portal
- Social/Community features (share progress)
- Chatbot AI nâng cao:
  - Service Python (FastAPI) train model phân loại intent (Naive Bayes/…)
  - Endpoint `POST /predict-intent` để Java gọi khi cần quyết định intent
  - Tự động gợi ý rule mới cho admin dựa trên dữ liệu chat
- CI/CD hoàn chỉnh (Docker image, staging deploy), monitoring (Prometheus/Grafana), alerting

---

## 🛠️ Công việc nền tảng & liên tục
- Git branching (main/develop/feature), PR template, code review checklist
- CI/CD: GitHub Actions chạy maven test + android tests, report coverage
- Monitoring/logging: Spring Boot Actuator, Logback JSON, chuẩn bị ELK/Cloud logging
- Security: secrets management (.env, GitHub secrets), HTTPS reverse proxy, OWASP scan
- Database migration: Flyway scripts cho mọi thay đổi schema
- QA/Test plan: regression list, smoke test sau mỗi deploy, accessibility checklist

---

## ✅ Checklist kết thúc dự án
- [ ] Hoàn tất deliverables 3 sprint + backlog ưu tiên
- [ ] API docs (Swagger + Postman) cập nhật/versioned
- [ ] README + RUNBOOK (deploy, config env, backup & restore)
- [ ] Admin portal & chatbot manual (huấn luyện rule, cấu hình Gemini)
- [ ] Release tags/changelog + kế hoạch rollback
- [ ] Đánh giá rủi ro & roadmap mở rộng (notification, AI nâng cao, analytics chuyên sâu)

---

**Ngày cập nhật:** 2025-11-09  
**Biên soạn:** Cursor GPT-5 Codex
