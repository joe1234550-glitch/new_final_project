package main.java.com.template.model.enums;

/**
 * 狀態列舉（範例）
 *
 * 📝 同學請替換成你的專題狀態，例如：
 *    - 預約：PENDING → CONFIRMED → CANCELLED
 *    - 房間：VACANT → OCCUPIED → CLEANING → INSPECTED
 *    - 任務：TODO → IN_PROGRESS → DONE
 */
public enum Status {
    ACTIVE("使用中", "🟢"),
    ARCHIVED("已封存", "📦"),
    DELETED("已刪除", "🗑️");

    private final String displayName;
    private final String icon;

    Status(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }

    public static Status fromString(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}
