# Hướng Dẫn Test API Bằng Postman

## 📋 Mục Lục
1. [Chuẩn Bị](#1-chuẩn-bị)
2. [Test Register API](#2-test-register-api)
3. [Test Login API](#3-test-login-api)
4. [Sử Dụng JWT Token](#4-sử-dụng-jwt-token)
5. [Tạo Collection Postman](#5-tạo-collection-postman)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Chuẩn Bị

### 1.1. Cài Đặt Postman

1. Tải Postman từ: https://www.postman.com/downloads/
2. Cài đặt và đăng nhập (hoặc tạo tài khoản miễn phí)

### 1.2. Khởi Động Ứng Dụng Spring Boot

1. Mở terminal/PowerShell
2. Di chuyển đến thư mục dự án:
   ```bash
   cd C:\Users\ADMIN\Downloads\be\hienpc
   ```
3. Chạy ứng dụng:
   ```bash
   mvn spring-boot:run
   ```
   hoặc
   ```bash
   ./mvnw spring-boot:run
   ```
4. Đợi ứng dụng khởi động (sẽ thấy: `Started BmichatbotApplication`)
5. Ứng dụng chạy tại: `http://localhost:8080`

### 1.3. Đảm Bảo MySQL Đang Chạy

- Mở XAMPP Control Panel
- Start MySQL service
- Đảm bảo database `bmi_chatbot_db` đã được tạo

---

## 2. Test Register API

### 2.1. Tạo Request Mới

1. Mở Postman
2. Click **"New"** → **"HTTP Request"**
3. Đặt tên request: `Register User`

### 2.2. Cấu Hình Request

**Method:** `POST`

**URL:** 
```
http://localhost:8080/api/auth/register
```

**Headers:**
- Click tab **"Headers"**
- Thêm header:
  - Key: `Content-Type`
  - Value: `application/json`

**Body:**
1. Click tab **"Body"**
2. Chọn **"raw"**
3. Chọn **"JSON"** từ dropdown
4. Nhập JSON body:

```json
{
  "email": "test@example.com",
  "password": "123456",
  "fullName": "Test User"
}
```

### 2.3. Gửi Request

1. Click nút **"Send"**
2. Xem kết quả trong phần **"Response"**

### 2.4. Response Thành Công (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNzMxMDg4MDAwLCJleHAiOjE3MzExNzQ0MDB9.xxxxx",
  "type": "Bearer",
  "email": "test@example.com",
  "userId": 1
}
```

**Giải thích:**
- `token`: JWT token (lưu lại để dùng cho các request sau)
- `type`: Loại token (Bearer)
- `email`: Email của user vừa đăng ký
- `userId`: ID của user trong database

### 2.5. Response Lỗi

**Email đã tồn tại (500):**
```json
{
  "timestamp": "2025-11-08T15:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Email already exists",
  "path": "/api/auth/register"
}
```

**Validation Error (400):**
```json
{
  "timestamp": "2025-11-08T15:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    },
    {
      "field": "password",
      "message": "Password must be at least 6 characters"
    }
  ],
  "path": "/api/auth/register"
}
```

---

## 3. Test Login API

### 3.1. Tạo Request Mới

1. Tạo request mới
2. Đặt tên: `Login User`

### 3.2. Cấu Hình Request

**Method:** `POST`

**URL:**
```
http://localhost:8080/api/auth/login
```

**Headers:**
- Key: `Content-Type`
- Value: `application/json`

**Body:**
- Chọn **"raw"** → **"JSON"**
- Nhập JSON:

```json
{
  "email": "test@example.com",
  "password": "123456"
}
```

### 3.3. Gửi Request

1. Click **"Send"**
2. Xem response

### 3.4. Response Thành Công (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNzMxMDg4MDAwLCJleHAiOjE3MzExNzQ0MDB9.xxxxx",
  "type": "Bearer",
  "email": "test@example.com",
  "userId": 1
}
```

### 3.5. Response Lỗi

**Sai email hoặc password (401):**
```json
{
  "timestamp": "2025-11-08T15:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Bad credentials",
  "path": "/api/auth/login"
}
```

**Validation Error (400):**
```json
{
  "timestamp": "2025-11-08T15:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ],
  "path": "/api/auth/login"
}
```

---

## 4. Sử Dụng JWT Token

### 4.1. Lưu Token Tự Động (Environment Variables)

1. Click **"Environments"** ở sidebar trái
2. Click **"+"** để tạo environment mới
3. Đặt tên: `BMI Chatbot Local`
4. Thêm variable:
   - **Variable:** `token`
   - **Initial Value:** (để trống)
   - **Current Value:** (để trống)
5. Click **"Save"**

### 4.2. Tự Động Lưu Token Sau Login/Register

**Cách 1: Sử dụng Tests Tab**

1. Mở request **"Register User"** hoặc **"Login User"**
2. Click tab **"Tests"**
3. Thêm script:

```javascript
// Lưu token vào environment variable
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    console.log("Token saved:", jsonData.token);
}
```

4. Sau khi gửi request thành công, token sẽ tự động lưu vào environment

**Cách 2: Copy Token Thủ Công**

1. Sau khi nhận response, copy token
2. Vào **"Environments"** → chọn environment
3. Paste token vào **Current Value** của variable `token`
4. Click **"Save"**

### 4.3. Sử Dụng Token Trong Request

Khi tạo các API protected (sẽ tạo sau), thêm header:

**Headers:**
- Key: `Authorization`
- Value: `Bearer {{token}}`

Postman sẽ tự động thay `{{token}}` bằng giá trị từ environment variable.

---

## 5. Tạo Collection Postman

### 5.1. Tạo Collection

1. Click **"New"** → **"Collection"**
2. Đặt tên: `BMI Chatbot API`
3. Click **"Create"**

### 5.2. Thêm Requests Vào Collection

1. Kéo thả các request vào collection
2. Hoặc click **"Add Request"** trong collection

### 5.3. Tổ Chức Collection

```
BMI Chatbot API
├── Authentication
│   ├── Register User
│   └── Login User
├── User Profile (sẽ thêm sau)
├── Body Measurements (sẽ thêm sau)
└── Chatbot (sẽ thêm sau)
```

### 5.4. Export Collection

1. Click **"..."** trên collection
2. Chọn **"Export"**
3. Chọn format: **"Collection v2.1"**
4. Click **"Export"**
5. Lưu file JSON

### 5.5. Import Collection

1. Click **"Import"** ở góc trên
2. Chọn file JSON đã export
3. Click **"Import"**

---

## 6. Troubleshooting

### 6.1. Lỗi: "Connection refused" hoặc "ECONNREFUSED"

**Nguyên nhân:** Ứng dụng Spring Boot chưa chạy

**Giải pháp:**
1. Kiểm tra ứng dụng đã chạy chưa
2. Kiểm tra port 8080 có bị chiếm không
3. Thử đổi port trong `application.properties`:
   ```properties
   server.port=8081
   ```

### 6.2. Lỗi: "401 Unauthorized" khi login

**Nguyên nhân:**
- Sai email hoặc password
- User chưa được tạo (chưa register)

**Giải pháp:**
1. Đảm bảo đã register user trước
2. Kiểm tra email và password chính xác
3. Thử register lại với email khác

### 6.3. Lỗi: "500 Internal Server Error" khi register

**Nguyên nhân:**
- Email đã tồn tại
- Database chưa kết nối
- Lỗi validation

**Giải pháp:**
1. Kiểm tra email chưa được sử dụng
2. Kiểm tra MySQL đang chạy
3. Kiểm tra database `bmi_chatbot_db` đã được tạo
4. Xem log trong console để biết lỗi chi tiết

### 6.4. Lỗi: "400 Bad Request" - Validation Error

**Nguyên nhân:**
- Email không đúng format
- Password quá ngắn (< 6 ký tự)
- Thiếu trường bắt buộc

**Giải pháp:**
1. Kiểm tra email đúng format (có @ và domain)
2. Password tối thiểu 6 ký tự
3. Đảm bảo tất cả trường bắt buộc đã điền

### 6.5. Token Không Hoạt Động

**Nguyên nhân:**
- Token đã hết hạn
- Token không đúng format
- Thiếu "Bearer " prefix

**Giải pháp:**
1. Login lại để lấy token mới
2. Kiểm tra token có "Bearer " prefix không
3. Kiểm tra token chưa hết hạn (mặc định 24 giờ)

---

## 7. Ví Dụ Request Hoàn Chỉnh

### 7.1. Register Request

**Method:** `POST`

**URL:** `http://localhost:8080/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "email": "john.doe@example.com",
  "password": "password123",
  "fullName": "John Doe"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "john.doe@example.com",
  "userId": 1
}
```

### 7.2. Login Request

**Method:** `POST`

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "john.doe@example.com",
  "userId": 1
}
```

---

## 8. Tips & Best Practices

### 8.1. Sử Dụng Environment Variables

- Tạo environment cho mỗi môi trường (Local, Dev, Production)
- Lưu base URL: `{{baseUrl}}/api/auth/register`
- Lưu token tự động sau login/register

### 8.2. Tạo Pre-request Scripts

**Ví dụ: Tự động set timestamp**

```javascript
// Pre-request Script
pm.environment.set("timestamp", new Date().toISOString());
```

### 8.3. Tạo Tests Tự Động

**Ví dụ: Kiểm tra response status**

```javascript
// Tests
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has token", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('token');
    pm.expect(jsonData.token).to.be.a('string');
});
```

### 8.4. Sử Dụng Variables Trong Collection

1. Click **"..."** trên collection
2. Chọn **"Edit"**
3. Tab **"Variables"**
4. Thêm variables:
   - `baseUrl`: `http://localhost:8080`
   - `apiVersion`: `v1`

5. Sử dụng: `{{baseUrl}}/api/auth/register`

---

## 9. Screenshots Hướng Dẫn

### 9.1. Tạo Request Mới

1. Click **"New"** → **"HTTP Request"**
2. Đặt tên request
3. Chọn method (POST)
4. Nhập URL

### 9.2. Cấu Hình Headers

1. Click tab **"Headers"**
2. Thêm `Content-Type: application/json`

### 9.3. Cấu Hình Body

1. Click tab **"Body"**
2. Chọn **"raw"**
3. Chọn **"JSON"**
4. Nhập JSON body

### 9.4. Xem Response

1. Click **"Send"**
2. Xem response ở tab **"Body"**
3. Xem status code và headers ở tab **"Headers"**

---

## 10. Quick Reference

### 10.1. Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Đăng ký user mới | No |
| POST | `/api/auth/login` | Đăng nhập | No |

### 10.2. Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Bad Request (Validation error) |
| 401 | Unauthorized (Wrong credentials) |
| 500 | Internal Server Error |

### 10.3. Common Headers

```
Content-Type: application/json
Authorization: Bearer <token>
```

---

**Ngày tạo:** 2025-11-08  
**Phiên bản:** 1.0

