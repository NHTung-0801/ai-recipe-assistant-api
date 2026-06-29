# 🍳 Home-Cooked Recipe Assistant API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Latest-green.svg)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)

> **🇬🇧 English:** A Spring Boot backend application powered by AI (Google Gemini) that helps users answer the daily question "What to eat today?" by suggesting healthy, home-cooked meals based on available ingredients.
>
> **🇻🇳 Tiếng Việt:** Một hệ thống backend Spring Boot tích hợp AI (Google Gemini) giúp giải quyết câu hỏi "Hôm nay ăn gì?" bằng cách đề xuất các bữa ăn tự nấu tốt cho sức khỏe dựa trên các nguyên liệu có sẵn trong tủ lạnh.

---

## 🚀 Features | Tính năng chính

* **Smart Recipe Suggestion (Gợi ý món ăn thông minh):** Analyzes input ingredients and generates 2-3 suitable recipes using Gemini AI. *(Phân tích nguyên liệu đầu vào và tạo 2-3 công thức phù hợp thông qua Gemini AI).*
* **Nutritional Tracking (Kiểm soát dinh dưỡng):** Estimates calories, protein, carbs, and fat for each suggested meal. *(Ước tính calo, đạm, tinh bột và chất béo cho từng bữa ăn).*
* **User History (Lịch sử người dùng):** Automatically saves user requests and AI-generated suggestions to MongoDB. *(Tự động lưu lại yêu cầu tìm kiếm và kết quả gợi ý vào MongoDB).*
* **Standardized API (Chuẩn hóa API):** Consistent JSON responses and global exception handling. *(Phản hồi JSON đồng nhất và xử lý lỗi tập trung toàn cục).*

---

## 🛠️ Technology Stack | Công nghệ sử dụng

* **Core:** Java 17, Spring Boot 3.2.x
* **Database:** MongoDB & Spring Data MongoDB
* **AI Integration:** Google Gemini Pro API (via Spring RestClient)
* **Deployment:** Docker & Docker Compose
* **Utilities:** Maven, Lombok, Validation

---

## ⚙️ Prerequisites | Yêu cầu hệ thống

To run this project, you will need / *Để chạy dự án này, bạn cần cài đặt:*
* [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or higher
* [Maven 3.9+](https://maven.apache.org/download.cgi)
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) (for running MongoDB locally)
* A valid **Google Gemini API Key**

---

## 📦 Installation & Setup | Hướng dẫn cài đặt

### 1. Clone the repository (Tải mã nguồn)
```bash
git clone [https://github.com/your-username/ai-recipe-assistant-api.git](https://github.com/your-username/ai-recipe-assistant-api.git)
cd ai-recipe-assistant-api
```

### 2. Environment Variables (Cấu hình biến môi trường)
Copy the .env.example file and create a new .env file in the root directory.
(Tạo một bản sao của file .env.example và đổi tên thành .env).
```bash
cp .env.example .env
```
Open the .env file and insert your actual GEMINI_API_KEY.
(Mở file .env và điền API Key thực tế của bạn vào).

### 3.Start the Database (Khởi động Cơ sở dữ liệu)
Ensure Docker is running, then execute:
(Đảm bảo Docker đang mở, sau đó chạy lệnh sau):
```bash
docker-compose up -d mongodb
```

### 4. Run the Application (Chạy ứng dụng)
```bash
mvn spring-boot:run
```
The server will start on http://localhost:8080.
(Server sẽ khởi chạy tại cổng 8080).


## 📂 Project Architecture | Cấu trúc thư mục
The project follows a clean architecture pattern with strict separation of concerns, ensuring high maintainability.
(Dự án tuân thủ kiến trúc phân tầng rõ ràng, đảm bảo tính dễ bảo trì và mở rộng).

```bash
src/main/java/com/project/recipe_assistant/
├── config/          # Configurations (Cấu hình hệ thống, RestClient, CORS)
├── constant/        # App Constants (Các hằng số dùng chung)
├── controller/      # API Endpoints (Nơi tiếp nhận Request)
├── dto/             # Data Transfer Objects (Định dạng dữ liệu giao tiếp)
├── exception/       # Global Error Handling (Bộ xử lý lỗi tập trung)
├── model/           # Database Entities (Định nghĩa cấu trúc MongoDB)
├── repository/      # Database Access (Giao tiếp với cơ sở dữ liệu)
├── service/         # Business Logic (Xử lý nghiệp vụ chính)
└── util/            # Utility Classes (Các công cụ hỗ trợ: PromptBuilder)
```


