package main.java.com.template.model.enums;

/**
 * 項目類別列舉（範例）
 *
 * 📝 同學請替換成你的專題需要的分類，例如：
 *    - 玫瑰：HYBRID_TEA, FLORIBUNDA, CLIMBING...
 *    - 房間：SINGLE, DOUBLE, SUITE, DELUXE...
 *    - 帳單：TELECOM, ELECTRICITY, MANAGEMENT_FEE...
 *    - RPG：WEAPON, ARMOR, POTION, MATERIAL...
 */
public enum Category {
    GENERAL("一般", "📋"),
    URGENT("緊急", "🔴"),
    IMPORTANT("重要", "⭐"),
    LOW("低優先", "🔽");

    private final String displayName;
    private final String icon;

    Category(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }

    /** 從字串轉換（資料庫讀取時使用） */
    public static Category fromString(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL;
        }
    }
}
