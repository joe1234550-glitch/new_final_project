package main.java.com.template.model;

import java.time.LocalDateTime;

public class Booking {
    private int id;
    private int userId;
    private int courtId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalFee;
    private String status;
    private LocalDateTime createdAt;

    // 用於顯示的額外資訊（非資料庫欄位，但 JOIN 時很有用）
    private String courtName;
    private String courtType;

    private String paymentMethod; // ONLINE, CASH
    /** 新增預約用 */
    public Booking(int userId, int courtId, LocalDateTime start, LocalDateTime end, int fee) {
        this.userId = userId;
        this.courtId = courtId;
        this.startTime = start;
        this.endTime = end;
        this.totalFee = fee;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    /** 從資料庫讀取用 */
    public Booking(int id, int userId, int courtId, LocalDateTime start, LocalDateTime end,
                   int fee, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.courtId = courtId;
        this.startTime = start;
        this.endTime = end;
        this.totalFee = fee;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Booking(int id, int userId, int courtId, LocalDateTime startTime, LocalDateTime endTime, int totalFee) {
        this.id = id;
        this.userId = userId;
        this.courtId = courtId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalFee = totalFee;
    }

    // Getters & Setters ...
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getCourtId() { return courtId; }
    public String getCourtName() { return courtName; }
    public void setCourtName(String name) { this.courtName = name; }
    public String getCourtType() { return courtType; }
    public void setCourtType(String type) { this.courtType = type; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getTotalFee() { return totalFee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String method) { this.paymentMethod = method; }
}
