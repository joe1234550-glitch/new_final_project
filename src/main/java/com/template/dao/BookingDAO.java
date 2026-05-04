package main.java.com.template.dao;
import main.java.com.template.config.DatabaseConfig;
import main.java.com.template.model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    /**
     * 建立預約單
     */
    public int create(Booking b) {
        String sql = "INSERT INTO bookings (user_id, court_id, start_time, end_time, total_fee, status) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getCourtId());
            ps.setTimestamp(3, Timestamp.valueOf(b.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(b.getEndTime()));
            ps.setInt(5, b.getTotalFee());
            ps.setString(6, "PENDING");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("❌ 建立預約失敗: " + e.getMessage());
        }
        return -1;
    }

    /**
     * 查詢特定會員的所有預約 (包含球場名稱顯現)
     */
    public List<Booking> findByUserId(int userId) {
        List<Booking> list = new ArrayList<>();
        // 使用 JOIN 同時抓出球場資訊
        String sql = "SELECT b.*, c.name as court_name, c.type as court_type " +
                "FROM bookings b " +
                "JOIN courts c ON b.court_id = c.id " +
                "WHERE b.user_id = ? ORDER BY b.start_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("court_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getInt("total_fee"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                // 額外設定球場資訊，這就是你要的「顯現第幾球場」
                b.setCourtName(rs.getString("court_name"));
                b.setCourtType(rs.getString("court_type"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("❌ 查詢預約紀錄失敗: " + e.getMessage());
        }
        return list;
    }
}
