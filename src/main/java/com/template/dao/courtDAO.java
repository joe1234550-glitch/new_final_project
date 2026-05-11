package main.java.com.template.dao;
import main.java.com.template.config.DatabaseConfig;
import main.java.com.template.model.court ;
import main.java.com.template.model.enums.CourtStatus;
import main.java.com.template.model.enums.CourtType;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
public class courtDAO {
    /**
     * 新增球場
     */
    public int create(court c) {
        String sql = "INSERT INTO courts (name, type, status, description, hourly_rate, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getType().name());
            ps.setString(3, c.getStatus().name());
            ps.setString(4, c.getDescription());
            ps.setInt(5, c.getHourlyRate());
            ps.setTimestamp(6, Timestamp.valueOf(c.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(c.getUpdatedAt()));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("❌ 新增球場失敗: " + e.getMessage());
        }
        return -1;
    }

    /**
     * 查詢所有球場
     */
    public List<court> findAll() {
        List<court> list = new ArrayList<>();
        String sql = "SELECT * FROM courts ORDER BY id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCourt(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ 查詢球場清單失敗: " + e.getMessage());
        }
        return list;
    }

    /**
     * 輔助方法：將資料庫內容轉回小寫的 court 物件
     */
    private court mapResultSetToCourt(ResultSet rs) throws SQLException {
        // 這裡調用你小寫 court 的建構子
        return new court(
                rs.getInt("id"),
                rs.getString("name"),
                CourtType.valueOf(rs.getString("type")),
                CourtStatus.valueOf(rs.getString("status")),
                rs.getString("description"),
                rs.getInt("hourly_rate"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    /**
     * 依 ID 查詢單一球場
     */
    public court findById(int id) {
        String sql = "SELECT * FROM courts WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCourt(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ 查詢球場失敗: " + e.getMessage());
        }
        return null;
    }

    public static boolean updateStatus(int id, CourtStatus status) {

        String sql = "UPDATE courts SET status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 將 Enum 轉為字串存入資料庫
            ps.setString(1, status.name());
            // 同步更新異動時間
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ courtDAO 更新狀態失敗: " + e.getMessage());
            return false;
        }
    }
    public boolean insert(court c) {
        String sql = "INSERT INTO courts (name, type, status, hourly_rate,description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getName());
            ps.setString(2,  c.getType().name());
            // 將 Enum 轉為字串存入資料庫
            ps.setString(3, c.getStatus().name());
            ps.setInt(4, c.getHourlyRate());
            ps.setString(5, c.getDescription()); // 寫入說明
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
