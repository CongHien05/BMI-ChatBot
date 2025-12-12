# API Documentation - BMI Chatbot Backend

> **Tài liệu này dành cho Frontend Team**  
> Cập nhật lần cuối: 2025-01-XX  
> Base URL: `http://localhost:8080`

---

## 📋 Mục Lục

1. [Authentication](#authentication)
2. [User Profile](#user-profile)
3. [Dashboard](#dashboard)
4. [Foods & Food Logs](#foods--food-logs)
5. [Exercises & Exercise Logs](#exercises--exercise-logs)
6. [Chatbot](#chatbot)
7. [Data Types](#data-types)
8. [Error Handling](#error-handling)

---

## 🔐 Authentication

### Base URL
```
/api/auth
```

### 1. Register User

**Endpoint:** `POST /api/auth/register`

**Description:** Đăng ký user mới và trả về JWT token

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "Nguyễn Văn A"
}
```

**Request Fields:**
- `email` (string, required): Email hợp lệ
- `password` (string, required): Mật khẩu tối thiểu 6 ký tự
- `fullName` (string, optional): Tên đầy đủ

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "user@example.com",
  "userId": 1
}
```

**Error Responses:**
- `400 Bad Request`: Email đã tồn tại hoặc validation failed
- `400 Bad Request`: Email format không hợp lệ
- `400 Bad Request`: Password quá ngắn (< 6 ký tự)

---

### 2. Login

**Endpoint:** `POST /api/auth/login`

**Description:** Đăng nhập và nhận JWT token

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Request Fields:**
- `email` (string, required): Email hợp lệ
- `password` (string, required): Mật khẩu

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "user@example.com",
  "userId": 1
}
```

**Error Responses:**
- `401 Unauthorized`: Email hoặc password sai
- `400 Bad Request`: Validation failed

---

## 👤 User Profile

### Base URL
```
/api/profile
```

### Update Profile

**Endpoint:** `PUT /api/profile`

**Description:** Cập nhật thông tin profile (partial update - chỉ cập nhật các field được gửi)

**Authentication:** Required (JWT Bearer Token)

**Request Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:** (Tất cả fields đều optional)
```json
{
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "goalType": "LOSE_WEIGHT",
  "goalWeightKg": 70.5,
  "dailyCalorieGoal": 2000
}
```

**Request Fields:**
- `dateOfBirth` (string, optional): Format `YYYY-MM-DD`
- `gender` (string, optional): `MALE`, `FEMALE`, hoặc `OTHER`
- `goalType` (string, optional): `LOSE_WEIGHT`, `GAIN_WEIGHT`, `MAINTAIN_WEIGHT`
- `goalWeightKg` (number, optional): Cân nặng mục tiêu (phải > 0)
- `dailyCalorieGoal` (integer, optional): Calorie mục tiêu mỗi ngày (phải >= 0)

**Response:** `200 OK` (No body)

**Error Responses:**
- `401 Unauthorized`: Token không hợp lệ hoặc hết hạn
- `400 Bad Request`: Validation failed (goalWeightKg <= 0 hoặc dailyCalorieGoal < 0)

---

## 📊 Dashboard

### Base URL
```
/api/dashboard
```

### Get Dashboard Summary

**Endpoint:** `GET /api/dashboard/summary`

**Description:** Lấy tổng quan dashboard (cân nặng hiện tại, BMI, tổng calories hôm nay)

**Authentication:** Required (JWT Bearer Token)

**Request Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response:** `200 OK`
```json
{
  "currentWeight": 75.5,
  "bmi": 24.2,
  "totalCaloriesToday": 1850
}
```

**Response Fields:**
- `currentWeight` (number, nullable): Cân nặng hiện tại (kg)
- `bmi` (number, nullable): Chỉ số BMI
- `totalCaloriesToday` (integer): Tổng calories đã nạp hôm nay

**Error Responses:**
- `401 Unauthorized`: Token không hợp lệ hoặc hết hạn

---

## 🍎 Foods & Food Logs

### Base URL
```
/api
```

### Get All Foods

**Endpoint:** `GET /api/foods`

**Description:** Lấy danh sách tất cả foods có sẵn

**Authentication:** Not required

**Response:** `200 OK`
```json
[
  {
    "foodId": 1,
    "foodName": "Cơm trắng",
    "servingUnit": "chén",
    "caloriesPerUnit": 200
  },
  {
    "foodId": 2,
    "foodName": "Thịt gà nướng",
    "servingUnit": "100g",
    "caloriesPerUnit": 165
  }
]
```

**Response Fields (mỗi item):**
- `foodId` (integer): ID của food
- `foodName` (string): Tên food
- `servingUnit` (string): Đơn vị tính (chén, 100g, etc.)
- `caloriesPerUnit` (integer): Calories mỗi đơn vị

---

### Log Food

**Endpoint:** `POST /api/logs/food`

**Description:** Ghi nhận food đã ăn

**Authentication:** Required (JWT Bearer Token)

**Request Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "foodId": 1,
  "quantity": 2.5,
  "mealType": "BREAKFAST"
}
```

**Request Fields:**
- `foodId` (integer, required): ID của food
- `quantity` (number, required): Số lượng (phải > 0.01)
- `mealType` (string, required): `BREAKFAST`, `LUNCH`, `DINNER`, hoặc `SNACK`

**Response:** `200 OK` (No body)

**Error Responses:**
- `401 Unauthorized`: Token không hợp lệ hoặc hết hạn
- `400 Bad Request`: Validation failed
  - `foodId` is required
  - `quantity` must be > 0.01
  - `mealType` is required

---

## 💪 Exercises & Exercise Logs

### Base URL
```
/api
```

### Get All Exercises

**Endpoint:** `GET /api/exercises`

**Description:** Lấy danh sách tất cả exercises có sẵn

**Authentication:** Not required

**Response:** `200 OK`
```json
[
  {
    "exerciseId": 1,
    "exerciseName": "Chạy bộ",
    "caloriesBurnedPerHour": 600
  },
  {
    "exerciseId": 2,
    "exerciseName": "Đi bộ",
    "caloriesBurnedPerHour": 300
  }
]
```

**Response Fields (mỗi item):**
- `exerciseId` (integer): ID của exercise
- `exerciseName` (string): Tên exercise
- `caloriesBurnedPerHour` (integer): Calories đốt cháy mỗi giờ

---

### Log Exercise

**Endpoint:** `POST /api/logs/exercise`

**Description:** Ghi nhận exercise đã thực hiện

**Authentication:** Required (JWT Bearer Token)

**Request Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "exerciseId": 1,
  "durationMinutes": 30
}
```

**Request Fields:**
- `exerciseId` (integer, required): ID của exercise
- `durationMinutes` (integer, required): Thời gian thực hiện (phút, phải >= 1)

**Response:** `200 OK` (No body)

**Error Responses:**
- `401 Unauthorized`: Token không hợp lệ hoặc hết hạn
- `400 Bad Request`: Validation failed
  - `exerciseId` is required
  - `durationMinutes` must be >= 1

---

## 🤖 Chatbot

### Base URL
```
/api/chatbot
```

### Chat with Bot

**Endpoint:** `POST /api/chatbot`

**Description:** Gửi message cho chatbot và nhận phản hồi (rule-based hoặc Gemini AI)

**Authentication:** Not required

**Request Body:**
```json
{
  "message": "Tôi muốn giảm cân, tôi nên làm gì?"
}
```

**Request Fields:**
- `message` (string, required): Message gửi cho chatbot

**Response:** `200 OK`
```json
{
  "reply": "Để giảm cân hiệu quả, bạn nên kết hợp chế độ ăn uống lành mạnh và tập thể dục đều đặn..."
}
```

**Response Fields:**
- `reply` (string): Phản hồi từ chatbot

**Error Responses:**
- `400 Bad Request`: `message` is required

---

## 📝 Data Types

### Request DTOs

#### LoginRequest
```typescript
{
  email: string;        // Required, valid email format
  password: string;     // Required
}
```

#### RegisterRequest
```typescript
{
  email: string;        // Required, valid email format
  password: string;     // Required, min 6 characters
  fullName?: string;    // Optional
}
```

#### ProfileUpdateRequest
```typescript
{
  dateOfBirth?: string;        // Optional, format: "YYYY-MM-DD"
  gender?: string;             // Optional: "MALE" | "FEMALE" | "OTHER"
  goalType?: string;           // Optional: "LOSE_WEIGHT" | "GAIN_WEIGHT" | "MAINTAIN_WEIGHT"
  goalWeightKg?: number;        // Optional, must be > 0
  dailyCalorieGoal?: number;    // Optional, must be >= 0
}
```

#### FoodLogRequest
```typescript
{
  foodId: number;       // Required
  quantity: number;     // Required, must be > 0.01
  mealType: string;     // Required: "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK"
}
```

#### ExerciseLogRequest
```typescript
{
  exerciseId: number;       // Required
  durationMinutes: number;  // Required, must be >= 1
}
```

#### ChatRequest
```typescript
{
  message: string;  // Required
}
```

---

### Response DTOs

#### JwtResponse
```typescript
{
  token: string;    // JWT token
  type: string;     // Always "Bearer"
  email: string;    // User email
  userId: number;   // User ID
}
```

#### DashboardSummary
```typescript
{
  currentWeight?: number;      // Nullable, current weight in kg
  bmi?: number;               // Nullable, BMI value
  totalCaloriesToday: number; // Total calories consumed today
}
```

#### FoodResponse
```typescript
{
  foodId: number;
  foodName: string;
  servingUnit: string;
  caloriesPerUnit: number;
}
```

#### ExerciseResponse
```typescript
{
  exerciseId: number;
  exerciseName: string;
  caloriesBurnedPerHour: number;
}
```

#### ChatResponse
```typescript
{
  reply: string;
}
```

---

## ⚠️ Error Handling

### Error Response Format

Khi có lỗi, backend trả về:

**400 Bad Request:**
```json
{
  "timestamp": "2025-01-XXT10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/login"
}
```

**401 Unauthorized:**
```json
{
  "timestamp": "2025-01-XXT10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token expired or invalid",
  "path": "/api/dashboard/summary"
}
```

**500 Internal Server Error:**
```json
{
  "timestamp": "2025-01-XXT10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/chatbot"
}
```

---

## 🔑 Authentication Flow

### 1. Register/Login
```
POST /api/auth/register hoặc /api/auth/login
→ Nhận JWT token trong response
```

### 2. Sử dụng Token
```
Thêm header vào mọi request cần authentication:
Authorization: Bearer <jwt-token>
```

### 3. Token Expiry
```
Nếu token hết hạn (401 Unauthorized):
→ Gọi lại /api/auth/login để lấy token mới
```

---

## 📌 Notes

1. **CORS:** Backend đã config CORS cho tất cả origins (`*`), nên FE có thể gọi API từ bất kỳ domain nào.

2. **Base URL:** 
   - Development: `http://localhost:8080`
   - Production: (sẽ cập nhật sau)

3. **Content-Type:** Tất cả requests đều dùng `application/json`

4. **Date Format:** Sử dụng `YYYY-MM-DD` cho dates (ISO 8601)

5. **Number Format:** 
   - Integers: `1`, `200`, `1000`
   - Decimals: `70.5`, `2.5`, `0.5`

---

## 🔗 Swagger UI

Để xem và test API trực tiếp trên browser:

**URL:** `http://localhost:8080/swagger-ui.html`

**OpenAPI Spec:**
- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`

---

## 📞 Support

Nếu có thắc mắc về API, vui lòng liên hệ Backend Team hoặc xem file `SWAGGER_OPENAPI_GUIDE.md` để biết thêm chi tiết.

---

**Tài liệu này được tự động cập nhật từ backend code.**
