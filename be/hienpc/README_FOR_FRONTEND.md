# 📚 Tài Liệu API cho Frontend Team

> **Đọc file này trước khi bắt đầu!**

---

## 🎯 Vấn Đề

Bạn đang làm việc với **2 cửa sổ Cursor riêng biệt** (Frontend và Backend) và muốn Frontend biết được Backend có gì mà **không cần gộp chung vào 1 cửa sổ**.

---

## ✅ Giải Pháp

Backend đã tạo sẵn các file sau để Frontend có thể sử dụng:

### 📄 **API_DOCUMENTATION.md**
**File quan trọng nhất!** Chứa đầy đủ thông tin về:
- Tất cả API endpoints
- Request/Response formats
- Error handling
- Authentication flow
- Examples

**👉 Đọc file này để biết Backend có API gì!**

---

### 🔷 **api-types.ts**
TypeScript type definitions cho tất cả DTOs.

**Cách dùng:**
1. Copy file này vào Frontend project: `src/types/api-types.ts`
2. Import trong code:
   ```typescript
   import { LoginRequest, JwtResponse } from './types/api-types';
   ```

---

### 📦 **openapi.json / openapi.yaml**
OpenAPI specification files (cần export từ server).

**Cách export:**
```powershell
# Windows
.\export-openapi.ps1

# Linux/Mac
./export-openapi.sh
```

**Dùng để:**
- Generate client code tự động
- Import vào Postman/Insomnia
- Xem trên Swagger Editor

---

### 📖 **FRONTEND_INTEGRATION_GUIDE.md**
Hướng dẫn chi tiết cách integrate Frontend với Backend.

**Nội dung:**
- Workflow khuyến nghị
- Cách generate client code
- Ví dụ code
- Best practices

---

## 🚀 Quick Start

### Bước 1: Đọc Tài Liệu
```
Mở: API_DOCUMENTATION.md
→ Xem tất cả APIs có sẵn
```

### Bước 2: Copy Types (Nếu dùng TypeScript)
```bash
# Copy file types vào Frontend
cp api-types.ts /path/to/frontend/src/types/
```

### Bước 3: Setup API Client
Xem ví dụ trong `FRONTEND_INTEGRATION_GUIDE.md`

### Bước 4: Test API
```
Mở browser: http://localhost:8080/swagger-ui.html
→ Test API trực tiếp trên browser
```

---

## 📋 Checklist

- [ ] Đọc `API_DOCUMENTATION.md`
- [ ] Copy `api-types.ts` vào Frontend (nếu cần)
- [ ] Đọc `FRONTEND_INTEGRATION_GUIDE.md` để biết cách integrate
- [ ] Test API trên Swagger UI
- [ ] Setup API client trong Frontend

---

## 🔗 Links Hữu Ích

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

---

## ❓ FAQ

**Q: Làm sao biết Backend có API gì?**  
A: Đọc `API_DOCUMENTATION.md` hoặc mở Swagger UI

**Q: Backend thêm API mới thì sao?**  
A: Backend sẽ update `API_DOCUMENTATION.md` và export OpenAPI spec mới. Frontend pull latest changes.

**Q: Có cần gộp chung workspace không?**  
A: Không! Chỉ cần copy các file documentation sang Frontend workspace.

**Q: Làm sao test API?**  
A: Dùng Swagger UI hoặc Postman/Insomnia với OpenAPI spec.

---

**Happy Coding! 🎉**
