package main.java.com.template.service;

import com.template.dao.ItemDAO;
import com.template.model.Item;
import com.template.model.enums.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * 項目服務 — 業務邏輯層
 *
 * 📝 Service 的職責：
 *    - 驗證資料（名稱不能空白、優先度範圍 1~5 等）
 *    - 組合多個 DAO 操作（例如：新增項目 + 寫入日誌）
 *    - 業務規則判斷（權限檢查、數量限制等）
 *
 * 📝 Service 不直接碰 Scanner 或 System.out（那是 View 的工作）
 *    Service 回傳結果讓 View 決定怎麼顯示。
 */
public class ItemService {

    private final ItemDAO itemDAO = new ItemDAO();

    // ==================== 新增 ====================

    /**
     * 新增項目（含驗證）
     * @return 錯誤訊息列表，空代表成功
     */
    public List<String> createItem(String name, String categoryStr, String description,
                                   int priority, int ownerId) {
        List<String> errors = new ArrayList<>();

        // 驗證：名稱不能為空
        if (name == null || name.isBlank()) {
            errors.add("名稱不能為空");
        }

        // 驗證：優先度 1~5
        if (priority < 1 || priority > 5) {
            errors.add("優先度必須在 1~5 之間");
        }

        // 驗證：類別必須合法
        Category category;
        try {
            category = Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            errors.add("無效的類別: " + categoryStr);
            return errors;
        }

        if (!errors.isEmpty()) return errors;

        // 通過驗證 → 寫入資料庫
        Item item = new Item(name, category, description, priority, ownerId);
        int newId = itemDAO.insert(item);

        if (newId < 0) {
            errors.add("資料庫寫入失敗");
        }
        return errors;
    }

    // ==================== 查詢 ====================

    /** 查詢使用者的所有項目 */
    public List<Item> getMyItems(int ownerId) {
        return itemDAO.findByOwner(ownerId);
    }

    /** 依類別篩選 */
    public List<Item> getByCategory(int ownerId, String category) {
        return itemDAO.findByCategory(ownerId, category);
    }

    /** 依 ID 查詢（含權限檢查） */
    public Item getById(int itemId, int userId) {
        Item item = itemDAO.findById(itemId);
        if (item == null) return null;
        // 權限檢查：只能查自己的（管理者例外，可在 View 層處理）
        if (item.getOwnerId() != userId) return null;
        return item;
    }

    /** 取得統計資料 */
    public String getStats(int ownerId) {
        return itemDAO.getStats(ownerId);
    }

    // ==================== 更新 ====================

    /** 更新項目（含權限檢查） */
    public boolean updateItem(Item item, int userId) {
        // 權限檢查
        if (item.getOwnerId() != userId) {
            System.out.println("❌ 你沒有權限修改此項目");
            return false;
        }
        return itemDAO.update(item);
    }

    // ==================== 刪除 ====================

    /** 刪除項目（軟刪除） */
    public boolean deleteItem(int itemId, int userId) {
        Item item = itemDAO.findById(itemId);
        if (item == null) return false;
        if (item.getOwnerId() != userId) {
            System.out.println("❌ 你沒有權限刪除此項目");
            return false;
        }
        return itemDAO.softDelete(itemId);
    }
}
