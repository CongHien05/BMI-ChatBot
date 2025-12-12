# Giải Thích Kiến Trúc Code Cốt Lõi - BMI Chatbot Project

## 📋 Mục Lục
1. [Cấu hình Database](#1-cấu-hình-database)
2. [Entity Classes](#2-entity-classes)
3. [Repository Pattern](#3-repository-pattern)
4. [Flow Hoạt Động](#4-flow-hoạt-động)
5. [Kiến Trúc Tổng Quan](#5-kiến-trúc-tổng-quan)

---

## 1. Cấu Hình Database

### File: `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bmi_chatbot_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

**Giải thích:**
- **Connection Pool (HikariCP)**: Spring Boot tự động tạo connection pool để quản lý kết nối MySQL
- **ddl-auto=update**: Hibernate tự động tạo/cập nhật bảng khi ứng dụng khởi động
  - Nếu bảng chưa có → Tạo mới
  - Nếu bảng đã có → Cập nhật schema nếu entity thay đổi

---

## 2. Entity Classes

### 2.1. User Entity

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.entity.User`

**Các trường:**
- `userId` (INT, PK, AI)
- `email` (VARCHAR, UNIQUE, NOT NULL)
- `password` (VARCHAR, NOT NULL)
- `fullName` (VARCHAR)

**Quan hệ:**
- **One-to-One** với `UserProfile` (mappedBy = "user")
- **One-to-Many** với `BodyMeasurement` (mappedBy = "user")

**Annotations quan trọng:**
```java
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private UserProfile userProfile;
```
- `mappedBy`: User không sở hữu foreign key, UserProfile có `user_id`
- `cascade = ALL`: Khi save/delete User → tự động save/delete UserProfile
- `orphanRemoval = true`: Xóa UserProfile nếu bị gỡ khỏi User

### 2.2. Admin Entity

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.entity.Admin`

**Các trường:**
- `adminId` (INT, PK, AI)
- `username` (VARCHAR, UNIQUE, NOT NULL)
- `password` (VARCHAR, NOT NULL)

**Đặc điểm:** Entity độc lập, không có quan hệ với entity khác

### 2.3. UserProfile Entity

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.entity.UserProfile`

**Các trường:**
- `profileId` (INT, PK, AI)
- `dateOfBirth` (DATE)
- `gender` (VARCHAR)
- `goalType` (VARCHAR)
- `goalWeightKg` (DECIMAL 5,2)
- `dailyCalorieGoal` (INT)

**Quan hệ:**
```java
@OneToOne
@JoinColumn(name = "user_id", unique = true, nullable = false)
private User user;
```
- `@JoinColumn`: UserProfile sở hữu foreign key `user_id`
- `unique = true`: Mỗi User chỉ có 1 UserProfile

### 2.4. BodyMeasurement Entity

**Vị trí:** `vn.vku.udn.hienpc.bmichatbot.entity.BodyMeasurement`

**Các trường:**
- `measurementId` (INT, PK, AI)
- `dateRecorded` (DATE)
- `weightKg` (DECIMAL 5,2)
- `heightCm` (DECIMAL 5,2)

**Quan hệ:**
```java
@ManyToOne
@JoinColumn(name = "user_id", nullable = false)
private User user;
```
- Nhiều BodyMeasurement thuộc 1 User
- BodyMeasurement có foreign key `user_id`

---

## 3. Repository Pattern

### 3.1. Cách Hoạt Động

**Spring Data JPA tự động implement:**
- `JpaRepository<User, Integer>` → Entity type = User, ID type = Integer
- Spring tự tạo implementation với các method:
  - `save()`, `findById()`, `findAll()`, `delete()`, `count()`, v.v.

### 3.2. Query Methods (Spring tự tạo)

**UserRepository:**
```java
Optional<User> findByEmail(String email);
// Tự tạo: SELECT * FROM users WHERE email = ?

boolean existsByEmail(String email);
// Tự tạo: SELECT COUNT(*) > 0 FROM users WHERE email = ?
```

**BodyMeasurementRepository:**
```java
List<BodyMeasurement> findByUserUserIdOrderByDateRecordedDesc(Integer userId);
// Tự tạo: SELECT * FROM body_measurements 
//         WHERE user_id = ? ORDER BY date_recorded DESC
```

**Quy tắc đặt tên:**
- `findBy` + `FieldName` → Tìm theo field
- `findBy` + `EntityName` + `FieldName` → Tìm qua quan hệ
- `OrderBy` + `FieldName` + `Desc` → Sắp xếp

---

## 4. Flow Hoạt Động

### 4.1. Khi Ứng Dụng Khởi Động

```
1. Spring Boot đọc application.properties
   ↓
2. Tạo DataSource (HikariCP) → Kết nối MySQL
   ↓
3. Tạo EntityManagerFactory (JPA/Hibernate)
   ↓
4. Hibernate quét tất cả @Entity classes
   ↓
5. So sánh Entity với Database schema
   ↓
6. Tạo/cập nhật bảng nếu cần (ddl-auto=update)
   ↓
7. Tạo các Repository beans (Spring tự implement)
   ↓
8. Ứng dụng sẵn sàng nhận request
```

### 4.2. Khi Service/Controller Sử Dụng Repository

**Ví dụ trong Service:**
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.save(user);  // INSERT INTO users ...
    }
}
```

**Flow:**
```
Controller → Service → Repository → JPA/Hibernate → MySQL Database
                                    ↓
                            Tự động convert:
                            - Java Object ↔ SQL Row
                            - Java Type ↔ SQL Type
                            - Relationship ↔ Foreign Key
```

### 4.3. Ví Dụ: Tạo User và UserProfile

```java
// 1. Tạo User
User user = new User();
user.setEmail("test@example.com");
user.setPassword("123456");
user = userRepository.save(user);  // INSERT users, userId = 1

// 2. Tạo UserProfile
UserProfile profile = new UserProfile();
profile.setUser(user);  // Set quan hệ
profile.setDateOfBirth(LocalDate.of(1990, 1, 1));
profile.setGender("Male");
profile = userProfileRepository.save(profile);  // INSERT user_profiles, user_id = 1
```

**Hibernate tự động:**
- Tạo foreign key `user_id = 1` trong `user_profiles`
- Đảm bảo unique constraint (mỗi User chỉ có 1 Profile)

---

## 5. Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────────┐
│              Controller Layer (API Endpoints)            │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              Service Layer (Business Logic)             │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│         Repository Layer (Data Access)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │UserRepository│  │AdminRepository│ │BodyMeasurement│ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│         JPA/Hibernate (ORM - Object Relational Mapping) │
│  - Convert Java Objects ↔ SQL                           │
│  - Manage Relationships                                 │
│  - Handle Transactions                                  │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              MySQL Database (XAMPP)                      │
│  - users, admins, user_profiles, body_measurements      │
└─────────────────────────────────────────────────────────┘
```

### Lợi Ích:
1. **Tách biệt concerns**: Controller → Service → Repository → Database
2. **ORM**: Không cần viết SQL thủ công
3. **Type-safe**: Compile-time checking
4. **Tự động quản lý quan hệ**: Foreign keys, cascades
5. **Dễ test**: Có thể mock Repository

---

## 📝 Ghi Chú

- **Lombok**: Sử dụng `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` để giảm boilerplate code
- **Cascade**: `CascadeType.ALL` + `orphanRemoval = true` để tự động xóa dữ liệu liên quan
- **JPA DDL Auto**: `update` mode tự động tạo/cập nhật schema
- **Database**: Đảm bảo database `bmi_chatbot_db` đã được tạo trước khi chạy ứng dụng

---

**Ngày tạo:** 2025-11-08  
**Phiên bản:** 1.0

