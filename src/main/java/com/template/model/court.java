package main.java.com.template.model;

import main.java.com.template.model.enums.CourtStatus;
import main.java.com.template.model.enums.CourtType;

import java.time.LocalDateTime;

public class court {
    private int id;
    private String name;
    private CourtType type;
    private CourtStatus status;
    private String description;
    private int hourlyRate;      // 每小時租金
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === 建構子 ===

    /** 新增球場用（還沒有 id） */
    public court(String name, CourtType type, String description, int hourlyRate) {
        this.name = name;
        this.type = type;
        this.status = CourtStatus.AVAILABLE; // 預設為可預約
        this.description = description;
        this.hourlyRate = hourlyRate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** 從資料庫讀取用（完整參數） */
    public court(int id, String name, CourtType type, CourtStatus status,
                 String description, int hourlyRate,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.description = description;
        this.hourlyRate = hourlyRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public court(String name, String typeStr, CourtStatus status, int hourlyRate) {
        this.name = name;
        // 將傳入的字串 (例如 "hard") 轉為 Enum (CourtType.HARD)
        this.type = CourtType.valueOf(typeStr.toUpperCase());
        this.status = status;
        this.hourlyRate = hourlyRate;
        // 自動初始化時間，這對資料庫寫入很重要
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    } //AdminService
    // === 業務方法 (Business Methods) ===

    /** 標記為已預約 */
    public void markAsBooked() {
        this.status = CourtStatus.BOOKED;
        updateTimestamp();
    }

    /** 釋放球場為可預約狀態 */
    public void release() {
        this.status = CourtStatus.AVAILABLE;
        updateTimestamp();
    }

    /** 設定為維修中 */
    public void startMaintenance() {
        this.status = CourtStatus.MAINTENANCE;
        updateTimestamp();
    }

    /** 內部方法：更新異動時間 */
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // === 顯示方法 (CLI Display) ===

    /** 完整資訊顯示 */
    public String display() {
        return String.format("%s [%d] %s %-10s  $%d/hr  %s",
                status.getIcon(), id, type.getIcon(), name, hourlyRate,
                description != null ? description : "");
    }

    /** 列表簡短顯示 */
    public String listDisplay() {
        return String.format("  %s [%d] %s %s", status.getIcon(), id, type.getIcon(), name);
    }

    @Override
    public String toString() {
        return display();
    }

    // === Getters & Setters ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CourtType getType() { return type; }
    public void setType(CourtType type) { this.type = type; }

    public CourtStatus getStatus() { return status; }
    public void setStatus(CourtStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(int hourlyRate) { this.hourlyRate = hourlyRate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

