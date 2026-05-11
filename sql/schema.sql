erDiagram
USERS ||--o{ BOOKINGS : "makes"
    COURTS ||--o{ BOOKINGS : "is reserved in"

    USERS {
        int id PK "Primary Key"
        string username "Unique"
        string password_hash
        string phone
        string email "Unique"
        string role "USER / ADMIN"
        timestamp created_at
        timestamp updated_at
    }

    COURTS {
        int id PK "Primary Key"
        string name "e.g., A號場"
        string type "HARD / CLAY / GRASS"
        string status "AVAILABLE / BOOKED / MAINTENANCE"
        string description
        int hourly_rate
        timestamp created_at
        timestamp updated_at
    }

    BOOKINGS {
        int id PK "Primary Key"
        int user_id FK "Foreign Key -> users.id"
        int court_id FK "Foreign Key -> courts.id"
        timestamp start_time
        timestamp end_time
        int total_fee
        string status "PENDING / PAID / CANCELLED"
        timestamp created_at
    }
-- 1. 會員資料表
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(30) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       phone VARCHAR(20),
                       email VARCHAR(100) UNIQUE,
                        -- 使用 CHECK 約束確保角色只能是 'USER' 或 'ADMIN'
                       role VARCHAR(10) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 球場資料表
CREATE TABLE courts (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(50) NOT NULL,
                        type VARCHAR(20) NOT NULL CHECK (type IN ('HARD', 'GRASS', 'CLAY')), -- 類型也可以順便鎖定
                        status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                            CHECK (status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE')),
                        description TEXT,
                        hourly_rate INT NOT NULL,        -- 時租金額
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 預約紀錄表 (多對多關聯表)
CREATE TABLE bookings (
                          id SERIAL PRIMARY KEY,
                          user_id INT NOT NULL,
                          court_id INT NOT NULL,
                          start_time TIMESTAMP NOT NULL,
                          end_time TIMESTAMP NOT NULL,
                          total_fee INT NOT NULL,
                          status VARCHAR(20) DEFAULT 'PENDING'
                              CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')), -- PENDING, PAID, CANCELLED
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          payment_method VARCHAR(20),

                          CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_court FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE
);
INSERT INTO courts (name, type, status, description, hourly_rate) VALUES
                                                                      ('A號場', 'HARD', 'AVAILABLE', '靠近門口，通風良好', 500),
                                                                      ('B號場', 'HARD', 'AVAILABLE', '標準硬地場', 500),
                                                                      ('C號場', 'CLAY', 'MAINTENANCE', '紅土整理中，暫不開放', 600),
                                                                      ('D號場', 'GRASS', 'AVAILABLE', '頂級草皮場', 800);

ALTER TABLE bookings ADD COLUMN payment_method VARCHAR(20); -- 'ONLINE', 'CASH'