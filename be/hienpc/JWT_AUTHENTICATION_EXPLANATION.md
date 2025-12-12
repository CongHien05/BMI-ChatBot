# Giải Thích JWT Authentication Implementation

## 📋 Mục Lục
1. [Tổng Quan](#1-tổng-quan)
2. [SecurityConfig - Cấu Hình Bảo Mật](#2-securityconfig---cấu-hình-bảo-mật)
3. [JwtService - Dịch Vụ JWT](#3-jwtservice---dịch-vụ-jwt)
4. [UserService - Dịch Vụ Người Dùng](#4-userservice---dịch-vụ-người-dùng)
5. [DTOs - Data Transfer Objects](#5-dtos---data-transfer-objects)
6. [AuthApiController - API Authentication](#6-authapicontroller---api-authentication)
7. [Flow Hoạt Động](#7-flow-hoạt-động)

---

## 1. Tổng Quan

Hệ thống JWT Authentication được xây dựng với các thành phần chính:

```
┌─────────────────────────────────────────────────────────┐
│              AuthApiController (API Endpoints)           │
│  - POST /api/auth/register                               │
│  - POST /api/auth/login                                  │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              UserService (Business Logic)                │
│  - loadUserByUsername()                                  │
│  - saveUser() - Hash password với BCrypt                │
│  - findByEmail()                                         │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              JwtService (Token Management)               │
│  - generateToken() - Tạo JWT token                       │
│  - validateToken() - Kiểm tra token hợp lệ              │
│  - extractUsername() - Lấy username từ token            │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              SecurityConfig (Security Configuration)     │
│  - Stateless configuration                              │
│  - Permit /api/auth/**                                   │
│  - Authenticate các request khác                         │
└─────────────────────────────────────────────────────────┘
```

---

## 2. SecurityConfig - Cấu Hình Bảo Mật

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.config.SecurityConfig`

### 2.1. Các Annotation

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
```

- **@Configuration**: Đánh dấu đây là class cấu hình Spring
- **@EnableWebSecurity**: Bật Spring Security cho web
- **@EnableMethodSecurity**: Cho phép sử dụng `@PreAuthorize`, `@Secured` ở method level

### 2.2. SecurityFilterChain - Cấu Hình HTTP Security

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Tắt CSRF cho API
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()  // Cho phép truy cập không cần auth
            .anyRequest().authenticated()  // Các request khác cần authenticate
        );
    return http.build();
}
```

**Giải thích:**
- **CSRF disabled**: API không cần CSRF protection (vì stateless)
- **STATELESS**: Không lưu session trên server, mỗi request độc lập
- **permitAll()**: Cho phép tất cả truy cập `/api/auth/**` (register, login)
- **authenticated()**: Các endpoint khác cần JWT token hợp lệ

### 2.3. AuthenticationProvider

```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
    authProvider.setUserDetailsService(userDetailsService());
    return authProvider;
}
```

**Giải thích:**
- **DaoAuthenticationProvider**: Xác thực dựa trên database
- **passwordEncoder()**: Sử dụng BCrypt để hash/verify password
- **userDetailsService()**: Load user từ database

### 2.4. PasswordEncoder - BCrypt

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**BCrypt:**
- Hash password một chiều (không thể decrypt)
- Tự động thêm salt (random) vào mỗi lần hash
- Mỗi lần hash cùng password → kết quả khác nhau
- Verify password bằng cách hash lại và so sánh

---

## 3. JwtService - Dịch Vụ JWT

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.service.JwtService`

### 3.1. Cấu Hình

```java
@Value("${jwt.secret:your-256-bit-secret-key-must-be-at-least-32-characters-long}")
private String secretKey;

@Value("${jwt.expiration:86400000}") // 24 hours default
private Long expiration;
```

**Giải thích:**
- **secretKey**: Key để ký và verify JWT token (lưu trong `application.properties`)
- **expiration**: Thời gian hết hạn token (mặc định 24 giờ = 86400000 ms)

### 3.2. Generate Token

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername());
}

private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
            .claims(claims)
            .subject(subject)  // Email của user
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())  // Ký token với secret key
            .compact();
}
```

**JWT Token Structure:**
```
Header.Payload.Signature

Header: {"alg": "HS256", "typ": "JWT"}
Payload: {"sub": "user@example.com", "iat": 1234567890, "exp": 1234654290}
Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

### 3.3. Validate Token

```java
public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}
```

**Quy trình validate:**
1. Extract username từ token
2. Kiểm tra username có khớp với userDetails không
3. Kiểm tra token chưa hết hạn

### 3.4. Extract Claims

```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}

private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())  // Verify signature
            .build()
            .parseSignedClaims(token)
            .getPayload();
}
```

---

## 4. UserService - Dịch Vụ Người Dùng

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.service.UserService`

### 4.1. Implement UserDetailsService

```java
@Service
public class UserService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())  // Đã được hash với BCrypt
                .authorities(new ArrayList<>())
                .build();
    }
}
```

**Giải thích:**
- **UserDetailsService**: Interface của Spring Security để load user
- **loadUserByUsername()**: Load user từ database bằng email
- Trả về **UserDetails** object để Spring Security sử dụng

### 4.2. Save User với Password Hashing

```java
public User saveUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));  // Hash password
    return userRepository.save(user);
}
```

**Quy trình:**
1. Nhận password plain text từ request
2. Hash password bằng BCrypt
3. Lưu user vào database với password đã hash

### 4.3. Helper Methods

```java
public User findByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
}

public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
}
```

---

## 5. DTOs - Data Transfer Objects

### 5.1. RegisterRequest

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.dto.request.RegisterRequest`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String fullName;
}
```

**Validation:**
- **@NotBlank**: Email và password không được để trống
- **@Email**: Email phải đúng format
- **@Size(min = 6)**: Password tối thiểu 6 ký tự

### 5.2. LoginRequest

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.dto.request.LoginRequest`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
```

### 5.3. JwtResponse

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.dto.response.JwtResponse`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;      // JWT token
    private String type = "Bearer";  // Token type
    private String email;     // User email
    private Integer userId;   // User ID
}
```

---

## 6. AuthApiController - API Authentication

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.controller.api.AuthApiController`

### 6.1. Register Endpoint

```java
@PostMapping("/register")
public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
    // 1. Kiểm tra email đã tồn tại chưa
    if (userService.existsByEmail(request.getEmail())) {
        throw new RuntimeException("Email already exists");
    }

    // 2. Tạo User mới
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(request.getPassword());  // Sẽ được hash trong saveUser()
    user.setFullName(request.getFullName());

    // 3. Lưu user (password tự động hash với BCrypt)
    User savedUser = userService.saveUser(user);

    // 4. Tạo JWT token
    UserDetails userDetails = userService.loadUserByUsername(savedUser.getEmail());
    String token = jwtService.generateToken(userDetails);

    // 5. Trả về response
    JwtResponse response = new JwtResponse(
            token,
            "Bearer",
            savedUser.getEmail(),
            savedUser.getUserId()
    );

    return ResponseEntity.ok(response);
}
```

**Flow:**
1. Validate request (email format, password length)
2. Kiểm tra email chưa tồn tại
3. Tạo User object
4. Save user (password được hash)
5. Generate JWT token
6. Trả về token và thông tin user

### 6.2. Login Endpoint

```java
@PostMapping("/login")
public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
    // 1. Authenticate user (Spring Security tự động verify password)
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
    );

    // 2. Lấy user details
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    User user = userService.findByEmail(userDetails.getUsername());

    // 3. Generate JWT token
    String token = jwtService.generateToken(userDetails);

    // 4. Trả về response
    JwtResponse response = new JwtResponse(
            token,
            "Bearer",
            user.getEmail(),
            user.getUserId()
    );

    return ResponseEntity.ok(response);
}
```

**Flow:**
1. Validate request
2. **AuthenticationManager** tự động:
   - Load user từ database
   - Verify password (so sánh BCrypt hash)
   - Nếu đúng → tạo Authentication object
   - Nếu sai → throw BadCredentialsException
3. Generate JWT token
4. Trả về token và thông tin user

---

## 7. Flow Hoạt Động

### 7.1. Flow Register

```
Client → POST /api/auth/register
         {email, password, fullName}
         ↓
AuthApiController.register()
         ↓
1. Validate request (@Valid)
2. Check email exists
3. Create User object
4. userService.saveUser() → Hash password với BCrypt
5. Save to database
6. Generate JWT token
7. Return JwtResponse {token, email, userId}
```

### 7.2. Flow Login

```
Client → POST /api/auth/login
         {email, password}
         ↓
AuthApiController.login()
         ↓
1. Validate request (@Valid)
2. authenticationManager.authenticate()
   ↓
   - Load user từ database (UserService.loadUserByUsername)
   - Verify password (BCrypt.compare)
   - Nếu đúng → Authentication object
   - Nếu sai → BadCredentialsException
3. Generate JWT token
4. Return JwtResponse {token, email, userId}
```

### 7.3. Flow Sử Dụng Token (Sau này)

```
Client → GET /api/protected-endpoint
         Header: Authorization: Bearer <token>
         ↓
JwtAuthenticationFilter (sẽ tạo sau)
         ↓
1. Extract token từ header
2. Validate token (JwtService.validateToken)
3. Extract username từ token
4. Load user từ database
5. Set Authentication vào SecurityContext
6. Cho phép request tiếp tục
```

---

## 8. Cấu Hình application.properties

```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-must-be-at-least-32-characters-long-for-security
jwt.expiration=86400000  # 24 hours in milliseconds
```

**Lưu ý:**
- **jwt.secret**: Nên thay đổi trong production, không commit vào Git
- **jwt.expiration**: Có thể điều chỉnh theo nhu cầu (ví dụ: 3600000 = 1 giờ)

---

## 9. Dependencies (pom.xml)

```xml
<!-- JWT Dependencies -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 10. Test API

### 10.1. Register

**Request:**
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456",
  "fullName": "Test User"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "test@example.com",
  "userId": 1
}
```

### 10.2. Login

**Request:**
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "test@example.com",
  "userId": 1
}
```

---

## 11. Lưu Ý Bảo Mật

1. **JWT Secret Key**: 
   - Không commit vào Git
   - Sử dụng environment variables hoặc secret management service
   - Độ dài tối thiểu 32 ký tự

2. **Password Hashing**:
   - Luôn hash password trước khi lưu database
   - Sử dụng BCrypt (mặc định 10 rounds)
   - Không bao giờ lưu plain text password

3. **Token Expiration**:
   - Đặt thời gian hết hạn hợp lý (24 giờ hoặc ngắn hơn)
   - Có thể implement refresh token cho UX tốt hơn

4. **HTTPS**:
   - Luôn sử dụng HTTPS trong production
   - JWT token có thể bị đánh cắp nếu dùng HTTP

---

**Ngày tạo:** 2025-11-08  
**Phiên bản:** 1.0

