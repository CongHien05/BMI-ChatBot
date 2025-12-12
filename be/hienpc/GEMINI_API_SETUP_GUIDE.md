# Gemini API Setup Guide - Hướng Dẫn Cấu Hình

## 📋 Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Lấy API Key](#lấy-api-key)
3. [Cấu Hình Backend](#cấu-hình-backend)
4. [Test Integration](#test-integration)
5. [Troubleshooting](#troubleshooting)

---

## 🎯 Tổng Quan

Chatbot BMI sử dụng **Google Gemini API** làm fallback khi không có rule nào match với user message.

### Kiến Trúc:
```
User Message
    ↓
1. Kiểm tra Rules (rule-based)
    ↓ (không match)
2. Gọi Gemini API (AI fallback)
    ↓
3. Trả về response từ Gemini
```

### Hiện Trạng:
- ✅ **Skeleton code** đã có (`GeminiApiService`)
- ⚠️ **Chưa tích hợp thật** (chỉ trả về fallback message)
- 📝 **Hướng dẫn này** mô tả cách tích hợp thật

---

## 🔑 Lấy API Key

### Bước 1: Truy Cập Google AI Studio
1. Mở browser, truy cập: https://aistudio.google.com/
2. Đăng nhập bằng Google account

### Bước 2: Tạo API Key
1. Click vào **"Get API Key"** hoặc **"Create API Key"**
2. Chọn project (hoặc tạo project mới)
3. Copy API key (dạng: `AIzaSy...`)

### Bước 3: Lưu API Key
- ⚠️ **KHÔNG commit API key vào Git!**
- Lưu vào file `.env` hoặc `application.properties` (local)
- Hoặc dùng environment variable

---

## ⚙️ Cấu Hình Backend

### Cách 1: Sử Dụng `application.properties` (Development)

1. **Mở file**: `src/main/resources/application.properties`

2. **Thêm config**:
```properties
# Gemini API Configuration
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
gemini.api.timeout=5000
```

3. **Lưu file** (⚠️ Đảm bảo file này không commit vào Git)

---

### Cách 2: Sử Dụng Environment Variable (Production)

1. **Set environment variable**:
```bash
# Linux/Mac
export GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE

# Windows (PowerShell)
$env:GEMINI_API_KEY="YOUR_GEMINI_API_KEY_HERE"
```

2. **Cập nhật `application.properties`**:
```properties
gemini.api.key=${GEMINI_API_KEY}
```

---

### Cách 3: Sử Dụng `.env` File (Recommended)

1. **Tạo file `.env`** ở root project:
```
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
```

2. **Thêm vào `.gitignore`**:
```
.env
```

3. **Load trong code** (cần thêm dependency `dotenv-java` nếu muốn)

---

## 💻 Implement GeminiApiService

### Hiện Tại (Skeleton):
```java
@Service
public class GeminiApiService {
    @Value("${gemini.api.key:YOUR_GEMINI_API_KEY}")
    private String geminiApiKey;

    public String ask(String query) {
        // Placeholder
        return "This is a fallback response from Gemini for message: \"" + query + "\"";
    }
}
```

### Cách Implement Thật:

#### Option 1: Sử Dụng RestTemplate (Simple)

```java
@Service
public class GeminiApiService {
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;
    
    private final RestTemplate restTemplate;
    
    public GeminiApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    public String ask(String query) {
        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
            return "Xin lỗi, chatbot hiện đang bảo trì. Vui lòng thử lại sau.";
        }
        
        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", query);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            // Parse response
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> contentResponse = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResponse.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            
            return "Xin lỗi, tôi không thể trả lời câu hỏi này. Vui lòng thử lại.";
            
        } catch (Exception e) {
            // Log error
            return "Xin lỗi, có lỗi xảy ra khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.";
        }
    }
}
```

#### Option 2: Sử Dụng Google Gemini Java SDK (Recommended)

1. **Thêm dependency vào `pom.xml`**:
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-aiplatform</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. **Implement service**:
```java
@Service
public class GeminiApiService {
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    public String ask(String query) {
        // Implementation using Google SDK
        // (Xem documentation của Google Gemini SDK)
    }
}
```

---

## 🧪 Test Integration

### Test 1: Kiểm Tra Config

1. **Khởi động ứng dụng**
2. **Kiểm tra log** xem API key có được load không:
```
gemini.api.key = AIzaSy...
```

### Test 2: Test API Call

1. **Gửi message không match rule nào**:
```bash
POST http://localhost:8080/api/chatbot
Authorization: Bearer <token>
Content-Type: application/json

{
  "message": "Xin chào, bạn có thể giúp tôi không?"
}
```

2. **Kiểm tra response**:
- Nếu có rule match → trả về rule response
- Nếu không có rule match → gọi Gemini API
- Nếu Gemini API hoạt động → trả về response từ Gemini
- Nếu Gemini API lỗi → trả về fallback message

### Test 3: Test Error Handling

1. **Test với API key sai**:
   - Set API key sai → Kiểm tra error handling
   
2. **Test với network error**:
   - Tắt internet → Kiểm tra timeout/error handling

---

## ⚠️ Troubleshooting

### Vấn Đề 1: API Key Không Hoạt Động

**Nguyên nhân:**
- API key sai hoặc hết hạn
- API key không có quyền truy cập Gemini API

**Giải pháp:**
1. Kiểm tra API key trong Google AI Studio
2. Tạo API key mới nếu cần
3. Đảm bảo API key có quyền truy cập Gemini API

---

### Vấn Đề 2: Timeout Error

**Nguyên nhân:**
- Network chậm
- Gemini API quá tải

**Giải pháp:**
1. Tăng timeout trong config:
```properties
gemini.api.timeout=10000
```

2. Thêm retry logic:
```java
@Retryable(value = {Exception.class}, maxAttempts = 3)
public String ask(String query) {
    // ...
}
```

---

### Vấn Đề 3: Response Format Không Đúng

**Nguyên nhân:**
- Gemini API response format thay đổi
- Parse response sai

**Giải pháp:**
1. Kiểm tra response structure từ Gemini API
2. Cập nhật code parse response
3. Thêm logging để debug

---

### Vấn Đề 4: Rate Limiting

**Nguyên nhân:**
- Gọi API quá nhiều lần
- Vượt quá quota

**Giải pháp:**
1. Thêm rate limiting:
```java
@RateLimiter(name = "gemini-api")
public String ask(String query) {
    // ...
}
```

2. Cache responses cho câu hỏi thường gặp
3. Upgrade plan nếu cần

---

## 💰 Pricing & Limits

### Free Tier:
- **60 requests/minute**
- **1,500 requests/day**

### Paid Tier:
- Xem tại: https://ai.google.dev/pricing

### Best Practices:
- ✅ Sử dụng rule-based trước (miễn phí)
- ✅ Chỉ gọi Gemini khi không có rule match
- ✅ Cache responses nếu có thể
- ✅ Monitor usage để tránh vượt quota

---

## 🔒 Security

### 1. **Bảo Mật API Key:**
- ⚠️ **KHÔNG commit API key vào Git**
- ✅ Sử dụng environment variables
- ✅ Sử dụng secrets management (AWS Secrets Manager, etc.)

### 2. **Rate Limiting:**
- Thêm rate limiting để tránh abuse
- Monitor API calls

### 3. **Input Validation:**
- Validate user input trước khi gửi đến Gemini
- Tránh injection attacks

---

## 📚 Tài Liệu Tham Khảo

- **Google Gemini API Docs**: https://ai.google.dev/docs
- **API Reference**: https://ai.google.dev/api
- **Java SDK**: https://github.com/googleapis/java-aiplatform

---

## 🎓 Tips

### 1. **Prompt Engineering:**
Có thể cải thiện response bằng cách thêm context vào prompt:
```java
String prompt = "Bạn là chatbot tư vấn sức khỏe BMI. " +
                "Trả lời ngắn gọn, thân thiện bằng tiếng Việt. " +
                "Câu hỏi: " + query;
```

### 2. **Context-Aware:**
Có thể thêm user context:
```java
String prompt = "User có BMI: " + userBmi + 
                ", Mục tiêu: " + userGoal + 
                ". Câu hỏi: " + query;
```

### 3. **Fallback Strategy:**
Nếu Gemini API fail, có thể:
- Trả về default message
- Gợi ý user thử lại
- Log error để admin xem

---

**Ngày tạo:** 2025-11-26  
**Phiên bản:** 1.0  
**Tác giả:** Cursor AI Assistant

**Lưu ý:** Hiện tại code chỉ là skeleton. Để tích hợp thật, cần implement `GeminiApiService.ask()` method theo hướng dẫn trên.

