package main.java.com.template.model;

import main.java.com.template.model.enums.Category;
import main.java.com.template.model.enums.Status;
import java.time.LocalDateTime;

/**
 * 項目模型（範例）
 *
 * 📝 對應資料表：items
 * 📝 同學請把這個類別重新命名成你的領域物件，例如：
 *    - Rose（玫瑰）
 *    - Room（房間）
 *    - Bill（帳單）
 *    - Quest（任務）
 *    - Court（球場）
 *    然後修改屬性（欄位）來符合你的需求。
 */
public class Item {
    private int id;
    private String name;
    private Category category;
    private Status status;
    private String description;
    private int priority;      // 1~5
    private int ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === 建構子 ===

    /** 新增用（還沒有 id） */
    public Item(String name, Category category, String description, int priority, int ownerId) {
        this.name = name;
        this.category = category;
        this.status = Status.ACTIVE;
        this.description = description;
        this.priority = priority;
        this.ownerId = ownerId;
    }

    /** 從資料庫讀取用 */
    public Item(int id, String name, Category category, Status status,
                String description, int priority, int ownerId,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.status = status;
        this.description = description;
        this.priority = priority;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // === 業務方法 ===

    /** 封存此項目 */
    public void archive() {
        this.status = Status.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    /** 標記刪除 */
    public void markDeleted() {
        this.status = Status.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    /** CLI 顯示格式 */
    public String display() {
        String stars = "★".repeat(priority) + "☆".repeat(5 - priority);
        return String.format("%s [%d] %s %s  %s  %s",
                status.getIcon(), id, category.getIcon(), name, stars, 
                description != null ? description : "");
    }

    /** 列表顯示（簡短） */
    public String listDisplay() {
        return String.format("  %s [%d] %s %s", status.getIcon(), id, category.getIcon(), name);
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
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public int getPriority() { return priority; }
    public void setPriority(int p) { this.priority = p; }
    public int getOwnerId() { return ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
