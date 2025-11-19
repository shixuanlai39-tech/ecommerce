## 功能列表

### 前台功能
- 使用者註冊 / 登入 / 登出
- 商品搜尋（支援關鍵字全文搜尋、分頁、）
- 商品詳情頁（圖片顯示、描述）
- 購物車（加入、刪除、調整數量，自動計算總金額）
- 結帳流程（簡單付款模擬）
- 忘記密碼（寄送重設密碼信）

### 後台管理功能（/admin）
- 商品管理（新增、編輯、上/下架、刪除、圖片上傳）

## 技術棧

- **後端**：Spring Boot 3.2.7（Java 17+）
- Spring Web MVC、Spring Data JPA、Spring Security、Spring Session、Spring Mail
- Spring Cache + Redis（熱門商品快取 + Session 集中管理）
- **資料庫**：MySQL 8.0
- **快取與 Session**：Redis 7.x（**必須啟動**）
- **前端**：Thymeleaf + Bootstrap 5 + jQuery（純後端渲染）
- **建置工具**：Maven
- **其他**：Lombok、Flyway（自動遷移）、Devtools

## 安裝與啟動步驟（務必先啟動 Redis！）

### 前置需求
- JDK 17 或更高版本
- Maven 3.8+
- MySQL 8.0
- Redis 6.2+（強制必須！專案使用 Redis 儲存 Session 與快取）
