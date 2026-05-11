# 📋 網球場預約系統

> 目的是為了讓球友可以預約某場館的網球場，並能顯示球場數量、預約時段、可預約場地、已預約場地
> 以及能夠線上進行信用卡支付或是可選擇現金支付，另外管理者也可以對系統進行新增、修改球場使用情況、取消預定的功能。

## 🏗️ 專案架構

```
project-template/
├── src/main/java/com/template/
│   ├── Main.java                    ← 程式入口
│   ├── config/
│   │   └── DatabaseConfig.java      ← JDBC 連線設定
│   ├── model/
│   │   ├── enums/
│   │   │   ├── CourtStatus.java        ← 類別列舉（請改成你的分類）
│   │   │   └── CourtType.java          ← 狀態列舉（請改成你的狀態流程）
│   │   ├── User.java                ← 使用者（可直接沿用）
│   │   ├── Booking.java 
│   │   └── court.java
│   ├── dao/
│   │   ├──courtDAO
│   │   ├──UserDAO.java             ← 使用者 CRUD（可直接沿用）
│   │   └──BookingDAO.java             ← 核心物件 CRUD（請改 SQL）
│   ├── service/
│   │   └── AdminService.java         ← 業務邏輯（驗證、權限）
│   └── view/
│       ├──AdminView.java
│       └── MainView.java            ← CLI 選單（請改選單文字）
├── sql/
│   └── schema.sql                   ← 建表 + 種子資料（請改表格）
├── run.sh                           ← Mac/Linux 一鍵執行
└── run.bat                          ← Windows 一鍵執行
```

## 🚀 如何使用

### 1. 建立資料庫

```bash
# 建立 PostgreSQL 資料庫（Docker 方式）
docker run -d --name mydb -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=myproject \
  postgres:16

# 匯入資料表
psql -U postgres -h localhost -d myproject -f sql/schema.sql
```

### 2. 編譯 & 執行

```bash
chmod +x run.sh
./run.sh
```

### 3. 測試帳號

| 帳號 | 密碼 | 角色 |
|------|------|------|
| admin | admin | 管理者 |
| demo | demo | 一般使用者 |

## 📐 架構說明

```
View（畫面）  →  Service（邏輯）  →  DAO（資料庫）  →  PostgreSQL
  ↑ Scanner         ↑ 驗證/判斷         ↑ SQL/JDBC
  ↓ println         ↓ 回傳結果         ↓ 回傳 Model
```

### 各層職責

| 層 | 職責 | 可以做 | 不能做 |
|----|------|--------|--------|
| **View** | 使用者互動 | Scanner / println / 選單 | 寫 SQL |
| **Service** | 業務邏輯 | 驗證 / 計算 / 呼叫 DAO | 碰 Scanner |
| **DAO** | 資料存取 | SQL / JDBC / 回傳 Model | 業務判斷 |
| **Model** | 資料結構 | 屬性 / Getter / 業務方法 | 碰資料庫 |


## 📊 類別圖（Mermaid）

```mermaid
classDiagram
    class User {
        - int id;
        - String username;
        - String role;   // "USER" or "ADMIN"
        - int totalItems;
        - String phone;
        - String email;
        +isAdmin() boolean
        +String toString()
    }
    class court {
        -int id
        -String name
        - CourtType type
        - CourtStatus status
        - String description
        - int hourlyRate      
        - LocalDateTime createdAt
        - LocalDateTime updatedAt
        +markAsBooked() void
        +release() void
        +startMaintenance()void
    }
    class Booking {
        - int id;
        - int userId;
        - int courtId;
        - LocalDateTime startTime;
        - LocalDateTime endTime;
        - int totalFee;
        - String status;
        - LocalDateTime createdAt;
        - String courtName;
        - String courtType;
        - String paymentMethod;
    }
    class CourtStatus {
        <<enumeration>>
        AVAILABLE
        BOOKED
        MAINTENANCE
    }
    class CourtType {
        <<enumeration>>
        AVAILABLE
        BOOKED
        MAINTENANCE     
    }
    class UserDAO {
        +register() int
        +login() User
        +findById() User
        +updateProfile() boolean
    }
    class courtDAO {
        +create() int
        +findAll() List~court~
        +findById() court
        +updateStatus() boolean
        +insert() boolean
        -mapResultSetToCourt()court
    }
    class BookingDAO{
        +create() int
        +findByUserId() List~Booking
        +updatePaymentInfo() boolean
        +isTimeSlotOccupied() boolean
        +findAll() List~Booking
        +findByCourtAndDate() List~Booking
    }
    class AdminService {
        +createCourt() List~String~
        +updateCourtStatus() boolean
        +getAllBookings() List~String~
        +processBooking() boolean
        +updateCourtStatus() boolean
    }
    class AdminView {
        -Scanner scanner
        -AdminService adminService
        -User adminUser
        +showMenu() void
        -handleAddCourt() void
        -displayAllBookings() void
        -handleBookingProcess() void
        -adminMenu() void
    }
    class MainView2 {
        -Scanner scanner
        -UserDAO userDAO
        -courtDAO courtDAO
        -BookingDAO bookingDAO
        -User currentUser
        -DateTimeFormatter formatter
        +MainView2(Scanner scanner)
        +start()
        -login()
        -register()
        -completeProfile()
        -mainMenu()
        -listCourts()
        -makeBooking()
        -showAvailableSlots(int courtId, LocalDate date)
        -paymentInterface(int bookingId, int fee)
        -listMyBookings()
        -printBanner()
        -readLine() String
        -readInt() int
    }

    court --> CourtStatus : 狀態標記
    court --> CourtType : 場地類型
    
    MainView2 --> UserDAO : 身份驗證
    MainView2 --> courtDAO : 查詢球場
    MainView2 --> BookingDAO : 建立預約
    MainView2 ..> User : 維護當前登入者
    MainView2 ..> AdminView : 權限分流跳轉
    
    AdminView --> AdminService : 請求業務處理
    AdminView ..> User : 識別管理員
    
    AdminService --> courtDAO : 更新場地資料
    AdminService ..> Booking : 處理訂單
    
    courtDAO ..> court : 封裝回傳
    BookingDAO ..> Booking : 封裝回傳
    UserDAO ..> User : 封裝回傳
```

## 📊 ERD（Mermaid）

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : "makes"
    COURTS ||--o{ BOOKINGS : "is booked for"

    USERS {
        int id PK
        string username
        string password_hash
        string role "ADMIN or USER"
        string phone
        string email
        int total_items
    }

    COURTS {
        int id PK
        string name
        string type "HARD, GRASS, CLAY"
        string status "AVAILABLE, MAINTENANCE"
        string description
        int hourly_rate
        timestamp created_at
        timestamp updated_at
    }

    BOOKINGS {
        int id PK
        int user_id FK
        int court_id FK
        timestamp start_time
        timestamp end_time
        int total_fee
        string status "PENDING, PAID, CANCELLED"
        string payment_method
        timestamp created_at
    }
```
## 📊 DEMO
![1.png](demo/1.png)
![2.png](demo/2.png)
![3.png](demo/3.png)
![4.png](demo/4.png)
![5.png](demo/5.png)
![6.png](demo/6.png)
![7.png](demo/7.png)