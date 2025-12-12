# Frontend Integration Guide

> **Hướng dẫn cho Frontend Team** - Cách sử dụng Backend API mà không cần gộp chung workspace

---

## 🎯 Mục Đích

File này hướng dẫn cách Frontend có thể biết được Backend có gì mà **không cần gộp chung vào 1 cửa sổ Cursor**.

---

## 📁 Các File Quan Trọng

### 1. **API_DOCUMENTATION.md**
File markdown chi tiết về tất cả APIs, endpoints, request/response formats.

**Cách sử dụng:**
- Copy file này vào workspace Frontend
- Hoặc mở file này từ workspace Backend khi cần tra cứu
- File này chứa đầy đủ thông tin về:
  - Tất cả endpoints
  - Request/Response formats
  - Error handling
  - Authentication flow

### 2. **api-types.ts**
TypeScript type definitions cho tất cả DTOs.

**Cách sử dụng:**
```bash
# Copy file này vào project Frontend
cp api-types.ts /path/to/frontend/src/types/api-types.ts
```

**Import trong code:**
```typescript
import { LoginRequest, JwtResponse, DashboardSummary } from './types/api-types';
```

### 3. **openapi.json / openapi.yaml**
OpenAPI specification files (cần export từ server đang chạy).

**Cách export:**
```powershell
# Windows (PowerShell)
.\export-openapi.ps1

# Linux/Mac (Bash)
chmod +x export-openapi.sh
./export-openapi.sh

# Hoặc manual:
curl http://localhost:8080/v3/api-docs -o openapi.json
curl http://localhost:8080/v3/api-docs.yaml -o openapi.yaml
```

**Cách sử dụng:**
- Import vào Postman/Insomnia để test API
- Generate client code (TypeScript, JavaScript, etc.)
- Sử dụng với Swagger Editor: https://editor.swagger.io/

---

## 🔄 Workflow Khuyến Nghị

### Option 1: Copy Files Sang Frontend Workspace

1. **Backend Team:**
   - Cập nhật code → Commit
   - Chạy script export OpenAPI: `.\export-openapi.ps1`
   - Commit `openapi.json`, `openapi.yaml`, `API_DOCUMENTATION.md`, `api-types.ts`

2. **Frontend Team:**
   - Pull latest changes từ Backend repo
   - Copy các files cần thiết vào Frontend workspace:
     ```bash
     # Copy API documentation
     cp backend/API_DOCUMENTATION.md frontend/docs/
     
     # Copy TypeScript types
     cp backend/api-types.ts frontend/src/types/
     
     # Copy OpenAPI spec (optional)
     cp backend/openapi.json frontend/docs/
     ```

### Option 2: Sử Dụng Shared Folder

Nếu cả 2 workspace đều có thể truy cập một folder chung:

1. Tạo folder chung: `C:\Shared\api-docs\` (hoặc `/shared/api-docs/`)
2. Backend export files vào folder này
3. Frontend đọc từ folder này

### Option 3: Sử Dụng Swagger UI (Real-time)

**Ưu điểm:** Luôn cập nhật real-time từ server đang chạy

1. Backend chạy server: `http://localhost:8080`
2. Frontend mở browser: `http://localhost:8080/swagger-ui.html`
3. Xem và test API trực tiếp trên browser

**Lưu ý:** Cần Backend server đang chạy

---

## 🛠️ Generate Client Code từ OpenAPI

### TypeScript/JavaScript (React, Vue, Angular)

```bash
# Cài đặt OpenAPI Generator
npm install -g @openapi-generators/openapi-generator-cli

# Generate TypeScript client
openapi-generator-cli generate \
  -i openapi.json \
  -g typescript-axios \
  -o frontend/src/api/generated

# Hoặc TypeScript fetch
openapi-generator-cli generate \
  -i openapi.json \
  -g typescript-fetch \
  -o frontend/src/api/generated
```

### React Query / TanStack Query

```bash
# Generate React Query hooks
openapi-generator-cli generate \
  -i openapi.json \
  -g typescript-react-query \
  -o frontend/src/api/generated
```

### Axios Client

```bash
# Generate Axios client
openapi-generator-cli generate \
  -i openapi.json \
  -g typescript-axios \
  -o frontend/src/api/generated
```

---

## 📝 Ví Dụ Sử Dụng

### 1. Setup API Client (TypeScript)

```typescript
// src/api/client.ts
import axios, { AxiosInstance } from 'axios';
import { 
  LoginRequest, 
  JwtResponse, 
  DashboardSummary,
  FoodResponse,
  ExerciseResponse 
} from '../types/api-types';

class ApiClient {
  private client: AxiosInstance;

  constructor(baseURL: string = 'http://localhost:8080') {
    this.client = axios.create({
      baseURL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add token to requests
    this.client.interceptors.request.use((config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Handle token expiry
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth APIs
  async login(request: LoginRequest): Promise<JwtResponse> {
    const response = await this.client.post<JwtResponse>('/api/auth/login', request);
    return response.data;
  }

  async register(request: RegisterRequest): Promise<JwtResponse> {
    const response = await this.client.post<JwtResponse>('/api/auth/register', request);
    return response.data;
  }

  // Dashboard APIs
  async getDashboardSummary(): Promise<DashboardSummary> {
    const response = await this.client.get<DashboardSummary>('/api/dashboard/summary');
    return response.data;
  }

  // Food APIs
  async getFoods(): Promise<FoodResponse[]> {
    const response = await this.client.get<FoodResponse[]>('/api/foods');
    return response.data;
  }

  // Exercise APIs
  async getExercises(): Promise<ExerciseResponse[]> {
    const response = await this.client.get<ExerciseResponse[]>('/api/exercises');
    return response.data;
  }
}

export const apiClient = new ApiClient();
```

### 2. Sử Dụng trong React Component

```typescript
// src/components/Login.tsx
import { useState } from 'react';
import { apiClient } from '../api/client';
import { LoginRequest } from '../types/api-types';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = async () => {
    try {
      const request: LoginRequest = { email, password };
      const response = await apiClient.login(request);
      
      // Save token
      localStorage.setItem('token', response.token);
      
      // Redirect to dashboard
      window.location.href = '/dashboard';
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      {/* Form fields */}
    </form>
  );
}
```

---

## 🔍 Cách Tra Cứu API

### Khi cần biết Backend có API gì:

1. **Mở `API_DOCUMENTATION.md`** → Tìm endpoint cần
2. **Hoặc mở Swagger UI** → `http://localhost:8080/swagger-ui.html`
3. **Hoặc xem `api-types.ts`** → Xem types để biết structure

### Khi Backend thêm API mới:

1. Backend commit code mới
2. Backend chạy `export-openapi.ps1` để update OpenAPI spec
3. Frontend pull latest changes
4. Frontend copy/update `api-types.ts` nếu cần
5. Frontend đọc `API_DOCUMENTATION.md` để xem API mới

---

## ✅ Checklist cho Frontend

- [ ] Copy `API_DOCUMENTATION.md` vào Frontend workspace
- [ ] Copy `api-types.ts` vào Frontend project
- [ ] Setup API client (axios/fetch) với base URL
- [ ] Implement authentication flow (login, save token, add to headers)
- [ ] Handle token expiry (401 → redirect to login)
- [ ] Test các APIs quan trọng (login, dashboard, etc.)

---

## 🚀 Tips

1. **Sử dụng TypeScript types** để có autocomplete và type safety
2. **Tạo API client wrapper** để dễ quản lý và reuse
3. **Sử dụng Swagger UI** để test API trước khi implement
4. **Đọc `API_DOCUMENTATION.md`** thay vì phải hỏi Backend team
5. **Export OpenAPI spec** định kỳ để có version mới nhất

---

## 📞 Support

Nếu có thắc mắc:
1. Đọc `API_DOCUMENTATION.md` trước
2. Xem Swagger UI: `http://localhost:8080/swagger-ui.html`
3. Liên hệ Backend team nếu cần

---

**Happy Coding! 🎉**
