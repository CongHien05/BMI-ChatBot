# Hướng Dẫn Push Code Lên GitHub

## 📋 Mục Lục
1. [Chuẩn Bị](#1-chuẩn-bị)
2. [Tạo Repository trên GitHub](#2-tạo-repository-trên-github)
3. [Push Code Lên GitHub](#3-push-code-lên-github)
4. [Các Lệnh Git Cơ Bản](#4-các-lệnh-git-cơ-bản)
5. [Troubleshooting](#5-troubleshooting)

---

## 1. Chuẩn Bị

### 1.1. Kiểm Tra Git Đã Cài Đặt

Mở PowerShell hoặc Command Prompt và chạy:
```bash
git --version
```

Nếu chưa cài, tải Git từ: https://git-scm.com/download/win

### 1.2. Cấu Hình Git (Lần Đầu)

```bash
git config --global user.name "Tên Của Bạn"
git config --global user.email "email@example.com"
```

---

## 2. Tạo Repository Trên GitHub

1. Đăng nhập vào GitHub: https://github.com
2. Click nút **"New"** hoặc **"+"** → **"New repository"**
3. Điền thông tin:
   - **Repository name**: `bmi-chatbot` (hoặc tên bạn muốn)
   - **Description**: "BMI Chatbot Spring Boot Application"
   - **Visibility**: Public hoặc Private
   - **KHÔNG** tích "Initialize with README" (vì đã có code)
4. Click **"Create repository"**

---

## 3. Push Code Lên GitHub

### Bước 1: Mở Terminal/PowerShell

Di chuyển đến thư mục dự án:
```bash
cd C:\Users\ADMIN\Downloads\be\hienpc
```

### Bước 2: Khởi Tạo Git Repository (Nếu Chưa Có)

```bash
git init
```

### Bước 3: Tạo File .gitignore (Nếu Chưa Có)

Tạo file `.gitignore` để bỏ qua các file không cần thiết:

```bash
# Tạo file .gitignore
New-Item -Path .gitignore -ItemType File
```

Nội dung file `.gitignore`:
```
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar
.mvn/

# IDE
.idea/
*.iml
.vscode/
*.class
*.log
*.jar
*.war
*.ear

# OS
.DS_Store
Thumbs.db

# Application
application-local.properties
*.log
```

### Bước 4: Thêm Tất Cả File Vào Git

```bash
git add .
```

### Bước 5: Commit Code

```bash
git commit -m "Initial commit: Setup database connection and core entities"
```

### Bước 6: Thêm Remote Repository

Thay `YOUR_USERNAME` và `YOUR_REPO_NAME` bằng thông tin của bạn:

```bash
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
```

**Ví dụ:**
```bash
git remote add origin https://github.com/hienpc/bmi-chatbot.git
```

### Bước 7: Push Code Lên GitHub

```bash
git branch -M main
git push -u origin main
```

Nếu GitHub yêu cầu authentication:
- Sử dụng **Personal Access Token** (PAT) thay vì password
- Tạo PAT tại: GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
- Quyền: `repo` (full control of private repositories)

---

## 4. Các Lệnh Git Cơ Bản

### 4.1. Kiểm Tra Trạng Thái

```bash
git status
```

### 4.2. Xem Lịch Sử Commit

```bash
git log
```

### 4.3. Thêm File Mới

```bash
git add <tên-file>
git add .  # Thêm tất cả
```

### 4.4. Commit Thay Đổi

```bash
git commit -m "Mô tả thay đổi"
```

### 4.5. Push Lên GitHub

```bash
git push origin main
```

### 4.6. Pull Code Từ GitHub

```bash
git pull origin main
```

### 4.7. Xem Remote Repository

```bash
git remote -v
```

### 4.8. Tạo Branch Mới

```bash
git checkout -b feature/new-feature
```

### 4.9. Chuyển Branch

```bash
git checkout main
```

---

## 5. Troubleshooting

### 5.1. Lỗi: "remote origin already exists"

```bash
git remote remove origin
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
```

### 5.2. Lỗi: "Authentication failed"

- Sử dụng **Personal Access Token** thay vì password
- Hoặc cấu hình SSH key

### 5.3. Lỗi: "Updates were rejected"

```bash
git pull origin main --rebase
git push origin main
```

### 5.4. Xóa File Đã Commit Nhầm

```bash
git rm <tên-file>
git commit -m "Remove file"
git push origin main
```

### 5.5. Hoàn Tác Commit (Chưa Push)

```bash
git reset --soft HEAD~1  # Giữ lại thay đổi
git reset --hard HEAD~1  # Xóa thay đổi
```

---

## 📝 Quy Trình Làm Việc Hàng Ngày

1. **Làm việc với code:**
   ```bash
   # Xem thay đổi
   git status
   
   # Thêm file
   git add .
   
   # Commit
   git commit -m "Mô tả thay đổi"
   
   # Push lên GitHub
   git push origin main
   ```

2. **Lấy code mới nhất:**
   ```bash
   git pull origin main
   ```

3. **Tạo branch cho tính năng mới:**
   ```bash
   git checkout -b feature/ten-tinh-nang
   # Làm việc...
   git add .
   git commit -m "Add new feature"
   git push origin feature/ten-tinh-nang
   ```

---

## 🔐 Bảo Mật

**QUAN TRỌNG:** Không commit các file nhạy cảm:
- `application.properties` với password thật
- API keys
- Private keys

**Giải pháp:**
- Sử dụng `application-local.properties` (đã ignore)
- Hoặc sử dụng environment variables
- Hoặc sử dụng Spring Cloud Config

---

**Ngày tạo:** 2025-11-08  
**Phiên bản:** 1.0

