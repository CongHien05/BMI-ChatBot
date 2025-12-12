# Chatbot Logging & Incident Runbook

> **Mục đích**: Tài liệu này hướng dẫn cách trace, debug và xử lý sự cố liên quan đến chatbot BMI.

---

## 📋 Mục lục

1. [Tổng quan về Logging Architecture](#tổng-quan-về-logging-architecture)
2. [Các nguồn Log](#các-nguồn-log)
3. [Cách Trace một Request](#cách-trace-một-request)
4. [Các Pattern Lỗi Thường Gặp](#các-pattern-lỗi-thường-gặp)
5. [SQL Queries Hữu Ích](#sql-queries-hữu-ích)
6. [Checklist Khi Điều Tra Sự Cố](#checklist-khi-điều-tra-sự-cố)
7. [Ví Dụ Thực Tế](#ví-dụ-thực-tế)

---

## 🏗️ Tổng quan về Logging Architecture

### Flow xử lý một Chat Request

```
1. User gửi message từ Android App
   ↓
2. POST /api/chatbot (ChatbotApiController)
   - Tạo traceId (UUID)
   - Log: "chat_request_received traceId=xxx messagePreview=..."
   ↓
3. ChatbotService.handleMessage()
   - Thử match rule-based (RULE)
   - Nếu không match → gọi GeminiApiService (GEMINI)
   ↓
4. Trả về ChatResponse
   - Lưu conversation + messages vào DB (ChatConversationService)
   - Log request vào chat_request_logs (ChatLoggingService)
   - Log: "chat_response_sent traceId=xxx source=RULE/GEMINI durationMs=..."
```

### Các thành phần Logging

| Component | Mục đích | Vị trí |
|-----------|----------|--------|
| **File Logs** | Structured logs với traceId, dễ grep | `logs/` hoặc console output |
| **chat_request_logs** | Bảng DB lưu metadata mỗi request (traceId, event, duration, source) | MySQL table |
| **chat_conversations** | Bảng DB lưu các cuộc hội thoại | MySQL table |
| **chat_messages** | Bảng DB lưu từng message (USER/BOT) | MySQL table |

---

## 📂 Các nguồn Log

### 1. File Logs (SLF4J/Logback)

**Vị trí**: Console output hoặc file log (nếu cấu hình Logback)

**Format**: Structured logs với key-value pairs

**Các log events quan trọng**:

```log
# Request nhận được
INFO  chat_request_received traceId=abc-123 messagePreview="Xin chào..."

# Rule match thành công
INFO  chat_rule_matched traceId=abc-123 ruleId=5 intent=GREETING

# Gọi Gemini API
INFO  chat_llm_called traceId=abc-123 model=Gemini messagePreview="..."

# Response gửi đi
INFO  chat_response_sent traceId=abc-123 source=RULE durationMs=15

# Request thất bại
WARN  chat_request_failed traceId=abc-123 error=Connection timeout
WARN  chat_request_failed_db traceId=abc-123 user=user@example.com error=...
```

**Cách grep**:

```bash
# Tìm tất cả logs của một traceId
grep "traceId=abc-123" application.log

# Tìm tất cả lỗi
grep "chat_request_failed" application.log

# Tìm tất cả requests trong 1 giờ qua
grep "chat_request_received" application.log | grep "2025-12-01 14:"
```

### 2. Database Tables

#### `chat_request_logs`

Lưu metadata của mỗi chat request.

**Cấu trúc**:

| Column | Type | Mô tả |
|--------|------|-------|
| `log_id` | BIGINT | Primary key |
| `trace_id` | VARCHAR(100) | UUID để trace request |
| `user_email` | VARCHAR(255) | Email user (nullable) |
| `event` | VARCHAR(50) | `CHAT_COMPLETED` hoặc `CHAT_FAILED` |
| `source` | VARCHAR(20) | `RULE` hoặc `GEMINI` (nullable nếu failed) |
| `duration_ms` | BIGINT | Thời gian xử lý (ms) |
| `error_message` | TEXT | Chi tiết lỗi (nullable) |
| `created_at` | DATETIME | Timestamp |

**Ví dụ dữ liệu**:

```sql
SELECT * FROM chat_request_logs WHERE trace_id = 'abc-123';
```

| log_id | trace_id | user_email | event | source | duration_ms | error_message | created_at |
|--------|----------|------------|-------|--------|-------------|---------------|------------|
| 1 | abc-123 | user@example.com | CHAT_COMPLETED | RULE | 15 | NULL | 2025-12-01 14:30:00 |

#### `chat_conversations`

Lưu các cuộc hội thoại (mỗi request tạo 1 conversation mới, tạm thời).

**Cấu trúc**:

| Column | Type | Mô tả |
|--------|------|-------|
| `conversation_id` | BIGINT | Primary key |
| `user_id` | BIGINT | FK đến `users` (nullable) |
| `channel` | VARCHAR(50) | `ANDROID`, `WEB`, ... |
| `started_at` | DATETIME | Thời điểm bắt đầu |
| `ended_at` | DATETIME | Thời điểm kết thúc (nullable) |

#### `chat_messages`

Lưu từng message trong conversation.

**Cấu trúc**:

| Column | Type | Mô tả |
|--------|------|-------|
| `message_id` | BIGINT | Primary key |
| `conversation_id` | BIGINT | FK đến `chat_conversations` |
| `sender` | VARCHAR(20) | `USER` hoặc `BOT` |
| `content` | TEXT | Nội dung message |
| `created_at` | DATETIME | Timestamp |

---

## 🔍 Cách Trace một Request

### Bước 1: Lấy traceId

**Từ Android App**:
- Khi user báo lỗi, yêu cầu họ copy `traceId` từ response (nếu có) hoặc từ logcat.
- Hoặc tra trong database dựa vào `user_email` và `created_at`.

**Từ Database**:
```sql
-- Tìm traceId của user gần nhất
SELECT trace_id, user_email, event, created_at 
FROM chat_request_logs 
WHERE user_email = 'user@example.com' 
ORDER BY created_at DESC 
LIMIT 10;
```

### Bước 2: Tra cứu File Logs

```bash
# Tìm tất cả logs liên quan đến traceId
grep "traceId=abc-123" application.log

# Hoặc nếu log ra file riêng
tail -f application.log | grep "abc-123"
```

**Kết quả mong đợi**:
```
INFO  chat_request_received traceId=abc-123 messagePreview="Xin chào..."
INFO  chat_rule_matched traceId=abc-123 ruleId=5 intent=GREETING
INFO  chat_response_sent traceId=abc-123 source=RULE durationMs=15
```

### Bước 3: Tra cứu Database

```sql
-- Xem log request
SELECT * FROM chat_request_logs WHERE trace_id = 'abc-123';

-- Xem conversation và messages
SELECT 
    c.conversation_id,
    c.channel,
    c.started_at,
    m.sender,
    m.content,
    m.created_at
FROM chat_conversations c
JOIN chat_messages m ON c.conversation_id = m.conversation_id
WHERE c.started_at >= (
    SELECT created_at FROM chat_request_logs WHERE trace_id = 'abc-123'
) - INTERVAL 1 MINUTE
  AND c.started_at <= (
    SELECT created_at FROM chat_request_logs WHERE trace_id = 'abc-123'
) + INTERVAL 1 MINUTE
ORDER BY m.created_at;
```

### Bước 4: Phân tích

- **Nếu `event = CHAT_COMPLETED`**: Request thành công, xem `source` (RULE/GEMINI) và `duration_ms`.
- **Nếu `event = CHAT_FAILED`**: Xem `error_message` để biết nguyên nhân.

---

## ⚠️ Các Pattern Lỗi Thường Gặp

### 1. Gemini API Timeout / Connection Error

**Triệu chứng**:
- `event = CHAT_FAILED`
- `error_message` chứa "timeout", "Connection refused", "Read timed out"
- `source = NULL` (chưa kịp set)

**Nguyên nhân**:
- Gemini API chậm hoặc không phản hồi
- Network issue giữa server và Gemini

**Cách xử lý**:
1. Kiểm tra network connectivity: `curl https://generativelanguage.googleapis.com`
2. Kiểm tra API key trong `application.properties` hoặc environment variable
3. Xem log chi tiết của `GeminiApiService`:
   ```bash
   grep "GeminiApiService" application.log | tail -20
   ```
4. Tăng timeout nếu cần (hiện tại mặc định của RestTemplate)

**SQL để tìm các lỗi này**:
```sql
SELECT trace_id, user_email, error_message, created_at
FROM chat_request_logs
WHERE event = 'CHAT_FAILED'
  AND (error_message LIKE '%timeout%' OR error_message LIKE '%Connection%')
ORDER BY created_at DESC
LIMIT 20;
```

---

### 2. Authentication Error (401/403)

**Triệu chứng**:
- Android app nhận `401 Unauthorized` hoặc `403 Forbidden`
- Không có log trong `chat_request_logs` (request bị reject trước khi vào controller)

**Nguyên nhân**:
- JWT token hết hạn hoặc không hợp lệ
- User chưa đăng nhập

**Cách xử lý**:
1. Kiểm tra JWT token trong request header:
   ```bash
   # Trong Android logcat hoặc Postman
   Authorization: Bearer <token>
   ```
2. Verify token tại `/api/auth/validate` (nếu có)
3. Yêu cầu user đăng nhập lại

**Log liên quan**:
```log
WARN  JwtAuthenticationFilter - Invalid JWT token
WARN  SecurityContext - Authentication failed
```

---

### 3. Rule Không Match (Fallback về Gemini)

**Triệu chứng**:
- `source = GEMINI` trong `chat_request_logs`
- User hỏi câu hỏi mà rule-based không cover

**Nguyên nhân**:
- Keywords trong rule không khớp với message của user
- Rule chưa được tạo cho intent này

**Cách xử lý**:
1. Xem message gốc của user:
   ```sql
   SELECT m.content
   FROM chat_messages m
   JOIN chat_conversations c ON m.conversation_id = c.conversation_id
   WHERE m.sender = 'USER'
     AND c.started_at >= (
       SELECT created_at FROM chat_request_logs WHERE trace_id = 'abc-123'
     ) - INTERVAL 1 MINUTE
   ORDER BY m.created_at DESC
   LIMIT 1;
   ```
2. Tạo rule mới trong Admin Portal (`/admin/rules`) với keywords phù hợp
3. Hoặc cải thiện keywords của rule hiện có

**SQL để tìm các requests dùng Gemini**:
```sql
SELECT trace_id, user_email, duration_ms, created_at
FROM chat_request_logs
WHERE source = 'GEMINI'
ORDER BY created_at DESC
LIMIT 50;
```

---

### 4. Database Connection Error

**Triệu chứng**:
- `event = CHAT_FAILED`
- `error_message` chứa "Unable to acquire JDBC connection", "Connection pool exhausted"

**Nguyên nhân**:
- MySQL server down hoặc không accessible
- Connection pool đầy (quá nhiều concurrent requests)

**Cách xử lý**:
1. Kiểm tra MySQL service:
   ```bash
   # Linux/Mac
   sudo systemctl status mysql
   
   # Windows
   services.msc → MySQL
   ```
2. Kiểm tra connection string trong `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/bmi_chatbot_db?...
   ```
3. Kiểm tra số lượng connections:
   ```sql
   SHOW PROCESSLIST;
   SHOW STATUS LIKE 'Threads_connected';
   ```
4. Tăng `spring.datasource.hikari.maximum-pool-size` nếu cần

---

### 5. Response Time Quá Chậm (>5 giây)

**Triệu chứng**:
- `duration_ms > 5000` trong `chat_request_logs`
- User phàn nàn chatbot chậm

**Nguyên nhân**:
- Gemini API chậm (network latency, API rate limit)
- Database query chậm (thiếu index, query N+1)
- Server overload

**Cách xử lý**:
1. Tìm các requests chậm:
   ```sql
   SELECT trace_id, user_email, source, duration_ms, created_at
   FROM chat_request_logs
   WHERE duration_ms > 5000
   ORDER BY duration_ms DESC
   LIMIT 20;
   ```
2. Phân tích:
   - Nếu `source = GEMINI`: Gemini API chậm → cân nhắc cache hoặc timeout ngắn hơn
   - Nếu `source = RULE`: Database query chậm → kiểm tra index trên `chatbot_rules`
3. Optimize:
   - Thêm index: `CREATE INDEX idx_priority ON chatbot_rules(priority);`
   - Cache rules trong memory (Spring Cache)

---

## 📊 SQL Queries Hữu Ích

### 1. Thống kê Requests theo Source

```sql
SELECT 
    source,
    COUNT(*) as total_requests,
    AVG(duration_ms) as avg_duration_ms,
    MAX(duration_ms) as max_duration_ms,
    MIN(duration_ms) as min_duration_ms
FROM chat_request_logs
WHERE event = 'CHAT_COMPLETED'
  AND created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY source;
```

### 2. Tỷ lệ Lỗi trong 24h qua

```sql
SELECT 
    DATE(created_at) as date,
    COUNT(*) as total,
    SUM(CASE WHEN event = 'CHAT_FAILED' THEN 1 ELSE 0 END) as failed,
    ROUND(100.0 * SUM(CASE WHEN event = 'CHAT_FAILED' THEN 1 ELSE 0 END) / COUNT(*), 2) as error_rate_percent
FROM chat_request_logs
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

### 3. Top Users có nhiều Requests nhất

```sql
SELECT 
    user_email,
    COUNT(*) as request_count,
    SUM(CASE WHEN event = 'CHAT_FAILED' THEN 1 ELSE 0 END) as failed_count
FROM chat_request_logs
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY user_email
ORDER BY request_count DESC
LIMIT 10;
```

### 4. Tìm Requests của một User trong khoảng thời gian

```sql
SELECT 
    trace_id,
    event,
    source,
    duration_ms,
    error_message,
    created_at
FROM chat_request_logs
WHERE user_email = 'user@example.com'
  AND created_at BETWEEN '2025-12-01 00:00:00' AND '2025-12-01 23:59:59'
ORDER BY created_at DESC;
```

### 5. Xem Full Conversation của một TraceId

```sql
-- Bước 1: Tìm conversation_id từ traceId (dựa vào thời gian)
SET @trace_time = (
    SELECT created_at FROM chat_request_logs WHERE trace_id = 'abc-123'
);

-- Bước 2: Lấy messages
SELECT 
    m.sender,
    m.content,
    m.created_at
FROM chat_messages m
JOIN chat_conversations c ON m.conversation_id = c.conversation_id
WHERE c.started_at BETWEEN DATE_SUB(@trace_time, INTERVAL 1 MINUTE) 
                      AND DATE_ADD(@trace_time, INTERVAL 1 MINUTE)
ORDER BY m.created_at;
```

### 6. Tìm các Requests có Error Message chứa keyword

```sql
SELECT 
    trace_id,
    user_email,
    error_message,
    created_at
FROM chat_request_logs
WHERE event = 'CHAT_FAILED'
  AND error_message LIKE '%timeout%'
ORDER BY created_at DESC
LIMIT 20;
```

---

## ✅ Checklist Khi Điều Tra Sự Cố

### Bước 1: Thu thập thông tin

- [ ] Lấy `traceId` từ user hoặc database
- [ ] Xác định thời gian xảy ra sự cố (timestamp)
- [ ] Xác định user bị ảnh hưởng (email)
- [ ] Xác định loại lỗi (timeout, 401, 500, ...)

### Bước 2: Tra cứu Logs

- [ ] Grep file logs với `traceId`
- [ ] Kiểm tra `chat_request_logs` table
- [ ] Kiểm tra `chat_conversations` và `chat_messages` nếu cần
- [ ] Xem log của các service liên quan (GeminiApiService, ChatbotService)

### Bước 3: Phân tích nguyên nhân

- [ ] Xác định `event` (CHAT_COMPLETED hay CHAT_FAILED)
- [ ] Xem `error_message` nếu có
- [ ] Kiểm tra `source` (RULE hay GEMINI)
- [ ] Kiểm tra `duration_ms` (có chậm bất thường không)

### Bước 4: Xử lý

- [ ] Nếu lỗi Gemini API: kiểm tra network, API key, timeout
- [ ] Nếu lỗi authentication: yêu cầu user đăng nhập lại
- [ ] Nếu rule không match: tạo/cập nhật rule trong Admin Portal
- [ ] Nếu database error: kiểm tra MySQL service, connection pool
- [ ] Nếu response chậm: optimize queries, thêm cache

### Bước 5: Ghi chép

- [ ] Ghi lại `traceId` và nguyên nhân vào ticket/issue
- [ ] Cập nhật runbook nếu phát hiện pattern mới
- [ ] Thông báo user nếu cần

---

## 💡 Ví Dụ Thực Tế

### Ví dụ 1: User báo "Chatbot không trả lời"

**Bước 1**: User cung cấp email: `user@example.com`, thời gian: `2025-12-01 14:30`

**Bước 2**: Tra cứu database:
```sql
SELECT trace_id, event, source, error_message, created_at
FROM chat_request_logs
WHERE user_email = 'user@example.com'
  AND created_at BETWEEN '2025-12-01 14:25:00' AND '2025-12-01 14:35:00'
ORDER BY created_at DESC;
```

**Kết quả**:
| trace_id | event | source | error_message | created_at |
|----------|-------|--------|---------------|------------|
| def-456 | CHAT_FAILED | NULL | Connection timeout: Read timed out | 2025-12-01 14:30:15 |

**Bước 3**: Phân tích:
- Lỗi timeout khi gọi Gemini API
- Network có vấn đề hoặc Gemini API chậm

**Bước 4**: Xử lý:
1. Kiểm tra network: `ping generativelanguage.googleapis.com`
2. Kiểm tra Gemini API key
3. Tăng timeout hoặc retry logic

---

### Ví dụ 2: User báo "Chatbot trả lời sai"

**Bước 1**: User cung cấp `traceId`: `ghi-789`

**Bước 2**: Tra cứu conversation:
```sql
SET @trace_time = (
    SELECT created_at FROM chat_request_logs WHERE trace_id = 'ghi-789'
);

SELECT m.sender, m.content, m.created_at
FROM chat_messages m
JOIN chat_conversations c ON m.conversation_id = c.conversation_id
WHERE c.started_at BETWEEN DATE_SUB(@trace_time, INTERVAL 1 MINUTE) 
                      AND DATE_ADD(@trace_time, INTERVAL 1 MINUTE)
ORDER BY m.created_at;
```

**Kết quả**:
| sender | content | created_at |
|--------|---------|------------|
| USER | "BMI của tôi là bao nhiêu?" | 2025-12-01 15:00:00 |
| BOT | "Xin chào, mình có thể giúp gì cho bạn?" | 2025-12-01 15:00:01 |

**Bước 3**: Phân tích:
- User hỏi về BMI nhưng bot trả lời greeting
- Rule "BMI" không match hoặc priority thấp

**Bước 4**: Xử lý:
1. Kiểm tra rule "BMI" trong Admin Portal
2. Cập nhật keywords: thêm "bmi", "chỉ số", "cân nặng"
3. Tăng priority nếu cần

---

## 📝 Ghi chú

- **TraceId format**: UUID v4 (ví dụ: `550e8400-e29b-41d4-a716-446655440000`)
- **Timezone**: Tất cả timestamps trong database dùng UTC (hoặc timezone của MySQL server)
- **Log retention**: File logs nên rotate hàng ngày, giữ tối thiểu 7 ngày
- **Database retention**: Cân nhắc archive các logs cũ (>30 ngày) sang bảng lịch sử

---

## 🔗 Liên kết

- [API Documentation](./API_DOCUMENTATION.md)
- [Admin Portal User Guide](./ADMIN_PORTAL_USER_GUIDE.md)
- [Chatbot Training Manual](./CHATBOT_TRAINING_MANUAL.md)

---

**Cập nhật lần cuối**: 2025-12-01  
**Tác giả**: Development Team

