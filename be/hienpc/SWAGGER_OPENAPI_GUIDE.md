# Swagger UI & OpenAPI Guide - Hướng Dẫn Sử Dụng

## 📋 Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Truy Cập Swagger UI](#truy-cập-swagger-ui)
3. [Sử Dụng Swagger UI](#sử-dụng-swagger-ui)
4. [OpenAPI Specification](#openapi-specification)
5. [Generate Client Code cho Android](#generate-client-code-cho-android)
6. [Cấu Hình Nâng Cao](#cấu-hình-nâng-cao)

---

## 🎯 Tổng Quan

Dự án đã tích hợp **SpringDoc OpenAPI** (Swagger UI) để:
- ✅ Tự động generate API documentation từ code
- ✅ Test API trực tiếp trên browser
- ✅ Export OpenAPI spec (YAML/JSON) để generate client code

### Công Nghệ:
- **SpringDoc OpenAPI**: Thư viện tự động generate OpenAPI spec từ Spring Boot annotations
- **Swagger UI**: Giao diện web để xem và test API
- **OpenAPI 3.0**: Chuẩn specification cho REST APIs

---

## 🌐 Truy Cập Swagger UI

### URL:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

### Yêu Cầu:
- ✅ Server đang chạy (`http://localhost:8080`)
- ✅ Không cần authentication để xem (đã được permit trong SecurityConfig)

---

## 📖 Sử Dụng Swagger UI

### 1. **Xem Danh Sách API**

Khi mở Swagger UI, bạn sẽ thấy:
- **Danh sách tất cả endpoints** được nhóm theo controller
- **Mô tả** của từng endpoint (từ `@Operation` annotation)
- **HTTP method** (GET, POST, PUT, DELETE)
- **Path** của endpoint

### 2. **Xem Chi Tiết Endpoint**

Click vào một endpoint để xem:
- **Mô tả** chi tiết
- **Parameters** (path, query, header, body)
- **Request body schema** (nếu có)
- **Response schemas** (200 OK, 400 Bad Request, etc.)
- **Example values**

### 3. **Test API Trực Tiếp**

#### Bước 1: Click nút **"Try it out"**
- Endpoint sẽ chuyển sang chế độ editable

#### Bước 2: Điền Parameters
- **Path parameters**: VD: `{id}` → nhập giá trị
- **Query parameters**: VD: `?page=1&size=10`
- **Request body**: Click "Example Value" hoặc tự nhập JSON

#### Bước 3: Authenticate (nếu cần)
- Click nút **"Authorize"** ở đầu trang
- Nhập JWT token: `Bearer <your-jwt-token>`
- Click **"Authorize"** → **"Close"**

#### Bước 4: Execute
- Click **"Execute"**
- Xem **Response** (status code, headers, body)

### 4. **Ví Dụ: Test Login API**

1. Tìm endpoint: **POST** `/api/auth/login`
2. Click **"Try it out"**
3. Điền request body:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
4. Click **"Execute"**
5. Xem response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "user@example.com",
  "userId": 1
}
```

### 5. **Ví Dụ: Test Protected API**

1. **Đăng nhập trước** để lấy JWT token (như trên)
2. Click **"Authorize"** ở đầu trang
3. Nhập: `Bearer <token-vừa-lấy>`
4. Click **"Authorize"** → **"Close"**
5. Test endpoint protected: VD: **GET** `/api/dashboard/summary`
6. Click **"Execute"** → Xem response

---

## 📄 OpenAPI Specification

### 1. **Xem OpenAPI Spec**

#### JSON Format:
```
http://localhost:8080/v3/api-docs
```

#### YAML Format:
```
http://localhost:8080/v3/api-docs.yaml
```

### 2. **Download Spec File**

#### Cách 1: Browser
- Mở URL trên → Right click → Save As
- Lưu với tên: `openapi.json` hoặc `openapi.yaml`

#### Cách 2: curl
```bash
# Download JSON
curl http://localhost:8080/v3/api-docs -o openapi.json

# Download YAML
curl http://localhost:8080/v3/api-docs.yaml -o openapi.yaml
```

### 3. **Nội Dung Spec File**

File OpenAPI spec chứa:
- ✅ **Info**: Title, description, version
- ✅ **Servers**: Base URLs
- ✅ **Paths**: Tất cả endpoints
- ✅ **Components**: Schemas (DTOs), Security schemes
- ✅ **Security**: Authentication methods (JWT Bearer)

---

## 🤖 Generate Client Code cho Android

### Cách 1: Sử Dụng OpenAPI Generator (Recommended)

#### Bước 1: Cài Đặt OpenAPI Generator

**Option A: Docker (Dễ nhất)**
```bash
docker pull openapitools/openapi-generator-cli
```

**Option B: npm**
```bash
npm install -g @openapi-generators/openapi-generator-cli
```

**Option C: Homebrew (Mac)**
```bash
brew install openapi-generator
```

#### Bước 2: Download OpenAPI Spec

```bash
# Download spec file
curl http://localhost:8080/v3/api-docs.yaml -o openapi.yaml
```

#### Bước 3: Generate Kotlin Client

```bash
# Sử dụng Docker
docker run --rm -v ${PWD}:/local openapitools/openapi-generator-cli generate \
  -i /local/openapi.yaml \
  -g kotlin \
  -o /local/android-client

# Hoặc sử dụng CLI
openapi-generator-cli generate \
  -i openapi.yaml \
  -g kotlin \
  -o android-client
```

#### Bước 4: Sử Dụng Generated Code

Generated code sẽ có:
- **API interfaces** (Retrofit-ready)
- **Data models** (DTOs)
- **Configuration** files

**Ví dụ sử dụng:**
```kotlin
// Generated API interface
val apiService = ApiClient.createService(AuthApi::class.java)

// Call API
val response = apiService.login(LoginRequest(email, password))
```

---

### Cách 2: Sử Dụng Swagger Codegen

#### Bước 1: Download Swagger Codegen
```bash
# Download từ: https://github.com/swagger-api/swagger-codegen
```

#### Bước 2: Generate Code
```bash
java -jar swagger-codegen-cli.jar generate \
  -i http://localhost:8080/v3/api-docs.yaml \
  -l kotlin \
  -o android-client
```

---

### Cách 3: Sử Dụng Online Tools

1. **Swagger Editor**: https://editor.swagger.io/
   - Paste OpenAPI spec
   - Generate → Client → Kotlin

2. **OpenAPI Generator Online**: https://openapi-generator.tech/
   - Upload spec file
   - Chọn Kotlin
   - Download generated code

---

## ⚙️ Cấu Hình Nâng Cao

### 1. **Cải Thiện OpenApiConfig**

Hiện tại config cơ bản. Có thể cải thiện:

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bmiChatbotOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("BMI Chatbot API")
                        .description("REST API specification for BMI Chatbot backend. " +
                                "Use this API to interact with the BMI tracking and chatbot features.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BMI Chatbot Team")
                                .email("support@bmichatbot.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.bmichatbot.com").description("Production")));
    }
}
```

### 2. **Thêm Tags để Nhóm Endpoints**

Trong controllers, thêm `@Tag`:

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthApiController {
    // ...
}
```

### 3. **Thêm Examples cho DTOs**

Trong DTOs, thêm `@Schema` với examples:

```java
@Schema(example = "user@example.com")
private String email;

@Schema(example = "password123", minLength = 6)
private String password;
```

### 4. **Customize Swagger UI Path**

Trong `application.properties`:

```properties
# Custom Swagger UI path
springdoc.swagger-ui.path=/swagger-ui.html

# Custom API docs path
springdoc.api-docs.path=/v3/api-docs

# Disable default petstore
springdoc.swagger-ui.disable-swagger-default-url=true
```

---

## 🔒 JWT Authentication trong Swagger UI

### Cấu Hình Hiện Tại:

OpenApiConfig đã có JWT security scheme. Để sử dụng:

1. **Mở Swagger UI**: `http://localhost:8080/swagger-ui.html`
2. **Click nút "Authorize"** (🔒) ở đầu trang
3. **Nhập JWT token**: `Bearer <your-token>`
4. **Click "Authorize"** → **"Close"**
5. **Test protected endpoints** → Token sẽ tự động được thêm vào header

### Lưu Ý:
- Token được lưu trong browser session
- Cần authorize lại nếu token hết hạn
- Có thể authorize nhiều security schemes cùng lúc

---

## 📊 API Documentation Structure

### Endpoints được nhóm theo:

1. **Authentication** (`/api/auth/**`)
   - `POST /api/auth/register` - Đăng ký user mới
   - `POST /api/auth/login` - Đăng nhập

2. **Foods** (`/api/foods`)
   - `GET /api/foods` - Lấy danh sách foods

3. **Food Logs** (`/api/logs/food`)
   - `POST /api/logs/food` - Log food intake

4. **Exercises** (`/api/exercises`)
   - `GET /api/exercises` - Lấy danh sách exercises

5. **Exercise Logs** (`/api/logs/exercise`)
   - `POST /api/logs/exercise` - Log exercise

6. **Dashboard** (`/api/dashboard/**`)
   - `GET /api/dashboard/summary` - Tổng quan dashboard

7. **Profile** (`/api/profile`)
   - `PUT /api/profile` - Cập nhật profile

8. **Chatbot** (`/api/chatbot`)
   - `POST /api/chatbot` - Gửi message cho chatbot

---

## 🎓 Best Practices

### 1. **Annotations trong Controllers:**

```java
@Operation(
    summary = "Đăng nhập user",
    description = "Authenticate user với email và password, trả về JWT token",
    tags = {"Authentication"}
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Đăng nhập thành công",
        content = @Content(schema = @Schema(implementation = JwtResponse.class))
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Email hoặc password sai"
    )
})
@PostMapping("/login")
public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
    // ...
}
```

### 2. **Annotations trong DTOs:**

```java
@Schema(description = "Request để đăng nhập")
public class LoginRequest {
    
    @Schema(description = "Email của user", example = "user@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Schema(description = "Mật khẩu", example = "password123", required = true, minLength = 6)
    @NotBlank(message = "Password is required")
    private String password;
}
```

### 3. **Response Examples:**

```java
@ApiResponse(
    responseCode = "200",
    description = "Success",
    content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(
            value = "{\"token\": \"eyJhbGci...\", \"type\": \"Bearer\", \"email\": \"user@example.com\", \"userId\": 1}"
        )
    )
)
```

---

## 🐛 Troubleshooting

### Vấn Đề 1: Swagger UI Không Load

**Nguyên nhân:**
- Static resources bị chặn
- Port không đúng

**Giải pháp:**
1. Kiểm tra SecurityConfig đã permit `/swagger-ui/**` và `/v3/api-docs/**`
2. Kiểm tra server đang chạy đúng port (8080)
3. Clear browser cache

---

### Vấn Đề 2: JWT Authentication Không Hoạt Động

**Nguyên nhân:**
- Security scheme chưa được config đúng
- Token format sai

**Giải pháp:**
1. Kiểm tra OpenApiConfig có security scheme không
2. Đảm bảo token format: `Bearer <token>` (có space sau Bearer)
3. Kiểm tra token còn hạn không

---

### Vấn Đề 3: OpenAPI Spec Không Đầy Đủ

**Nguyên nhân:**
- Thiếu annotations
- DTOs không có schema

**Giải pháp:**
1. Thêm `@Operation` cho mỗi endpoint
2. Thêm `@Schema` cho DTOs
3. Thêm `@Tag` để nhóm endpoints

---

## 📚 Tài Liệu Tham Khảo

- **SpringDoc OpenAPI**: https://springdoc.org/
- **OpenAPI Specification**: https://swagger.io/specification/
- **OpenAPI Generator**: https://openapi-generator.tech/
- **Swagger UI**: https://swagger.io/tools/swagger-ui/

---

## 🎯 Checklist cho FE Team

### Để FE có thể sử dụng API:

- [x] ✅ Swagger UI accessible tại `/swagger-ui.html`
- [x] ✅ OpenAPI spec accessible tại `/v3/api-docs`
- [x] ✅ JWT authentication được config trong Swagger UI
- [ ] ⚠️ OpenAPI spec file được export và commit vào repo (optional)
- [ ] ⚠️ Hướng dẫn generate client code (đã có trong guide này)

### FE có thể:
1. **Xem tất cả APIs** trên Swagger UI
2. **Test APIs** trực tiếp trên browser
3. **Download OpenAPI spec** để generate client code
4. **Sử dụng generated code** trong Android project

---

**Ngày tạo:** 2025-11-26  
**Phiên bản:** 1.0  
**Tác giả:** Cursor AI Assistant

