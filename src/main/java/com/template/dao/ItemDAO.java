package main.java.com.template.dao;

import main.java.com.template.config.DatabaseConfig;
import main.java.com.template.model.Item;
import main.java.com.template.model.enums.Category;
import main.java.com.template.model.enums.Status;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 項目 DAO — 完整的 CRUD 範例
 *
 * 📝 這個類別展示了 DAO 的標準 CRUD 寫法：
 *    - Create → insert()
 *    - Read   → findById() / findAll() / findByOwner()
 *    - Update → update()
 *    - Delete → delete()
 *
 * 📝 同學請把 Item 換成你的領域物件，SQL 換成你的資料表。
 */
public class ItemDAO {

    // ==================== CREATE ====================

    /** 新增項目，回傳新 ID */
    public int insert(Item item) {
        String sql = """
            INSERT INTO items (name, category, status, description, priority, owner_id)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory().name());   // Enum → 字串
            ps.setString(3, item.getStatus().name());
            ps.setString(4, item.getDescription());
            ps.setInt(5, item.getPriority());
            ps.setInt(6, item.getOwnerId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                item.setId(newId);
                return newId;
            }
        } catch (SQLException e) {
            System.err.println("❌ 新增失敗: " + e.getMessage());
        }
        return -1;
    }

    // ==================== READ ====================

    /** 依 ID 查詢 */
    public Item findById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ 查詢失敗: " + e.getMessage());
        }
        return null;
    }

    /** 查詢某使用者的所有項目（排除已刪除） */
    public List<Item> findByOwner(int ownerId) {
        String sql = "SELECT * FROM items WHERE owner_id = ? AND status != 'DELETED' ORDER BY priority DESC, created_at DESC";
        return executeQuery(sql, ownerId);
    }

    /** 查詢全部（管理者用） */
    public List<Item> findAll() {
        String sql = "SELECT * FROM items WHERE status != 'DELETED' ORDER BY priority DESC, created_at DESC";
        return executeQuery(sql, -1);
    }

    /** 依類別篩選 */
    public List<Item> findByCategory(int ownerId, String category) {
        String sql = "SELECT * FROM items WHERE owner_id = ? AND category = ? AND status != 'DELETED' ORDER BY priority DESC";
        List<Item> items = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setString(2, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("❌ 篩選失敗: " + e.getMessage());
        }
        return items;
    }

    /** 統計某使用者各狀態的數量 */
    public String getStats(int ownerId) {
        String sql = "SELECT status, COUNT(*) as cnt FROM items WHERE owner_id = ? GROUP BY status";
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Status s = Status.fromString(rs.getString("status"));
                sb.append(String.format("  %s %s: %d 筆%n", s.getIcon(), s.getDisplayName(), rs.getInt("cnt")));
            }
        } catch (SQLException e) {
            System.err.println("❌ 統計失敗: " + e.getMessage());
        }
        return sb.toString();
    }

    // ==================== UPDATE ====================

    /** 更新項目 */
    public boolean update(Item item) {
        String sql = """
            UPDATE items SET name = ?, category = ?, status = ?, description = ?, priority = ?, updated_at = NOW()
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory().name());
            ps.setString(3, item.getStatus().name());
            ps.setString(4, item.getDescription());
            ps.setInt(5, item.getPriority());
            ps.setInt(6, item.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ 更新失敗: " + e.getMessage());
        }
        return false;
    }

    // ==================== DELETE ====================

    /** 軟刪除（改狀態，不真正刪除資料） */
    public boolean softDelete(int id) {
        String sql = "UPDATE items SET status = 'DELETED', updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ 刪除失敗: " + e.getMessage());
        }
        return false;
    }

    // ==================== 共用方法 ====================

    /** 🔧 將 ResultSet 轉換為 Item 物件（每個 DAO 都需要這個方法） */
    private Item mapRow(ResultSet rs) throws SQLException {
        return new Item(
            rs.getInt("id"),
            rs.getString("name"),
            Category.fromString(rs.getString("category")),
            Status.fromString(rs.getString("status")),
            rs.getString("description"),
            rs.getInt("priority"),
            rs.getInt("owner_id"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }

    /** 🔧 執行查詢並回傳 List（減少重複程式碼） */
    private List<Item> executeQuery(String sql, int paramId) {
        List<Item> items = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (paramId > 0) ps.setInt(1, paramId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("❌ 查詢失敗: " + e.getMessage());
        }
        return items;
    }
}
