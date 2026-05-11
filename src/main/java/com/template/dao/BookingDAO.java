package main.java.com.template.dao;
import main.java.com.template.config.DatabaseConfig;
import main.java.com.template.model.Booking;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public boolean updatePaymentInfo(int bookingId, String method, String status) {
        String sql = "UPDATE bookings SET payment_method = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, method);
            ps.setString(2, status);
            ps.setInt(3, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ 更新支付資訊失敗: " + e.getMessage());
            return false;
        }
    }
    public boolean isTimeSlotOccupied(int courtId, LocalDateTime start, LocalDateTime end) {
        // 邏輯：尋找是否有任何現有預約的時段與新預約重疊
        // 公式：(新開始 < 現有結束) AND (新結束 > 現有開始)
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE court_id = ? " +
                "AND status NOT IN ('CANCELLED') " + // 排除已取消的預約
                "AND (start_time < ? AND end_time > ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courtId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(end));   // 傳入新結束時間
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(start)); // 傳入新開始時間

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ 檢查時段衝突失敗: " + e.getMessage());
        }
        return false;
    }
    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        // SQL 強化：同時關聯 users 拿帳號，關聯 courts 拿場地名
        String sql = "SELECT b.*, u.username, c.name as court_name " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.id " +
                "JOIN courts c ON b.court_id = c.id " +
                "ORDER BY b.start_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("court_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getInt("total_fee")
                );
                b.setStatus(rs.getString("status"));
                b.setUsername(rs.getString("username"));   // 設定帳號
                b.setCourtName(rs.getString("court_name")); // 設定場地名
                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<Booking> findByCourtAndDate(int courtId, LocalDate date) {
        List<Booking> list = new ArrayList<>();
        // 查詢當天 00:00:00 到 23:59:59 之間的所有預約
        String sql = "SELECT * FROM bookings WHERE court_id = ? " +
                "AND start_time >= ? AND start_time < ? " +
                "AND status != 'CANCELLED' ORDER BY start_time ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courtId);
            ps.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Booking(
                        rs.getInt("id"), rs.getInt("user_id"), rs.getInt("court_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getInt("total_fee")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public Booking findById(int id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("court_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getInt("total_fee")
                );
                b.setStatus(rs.getString("status"));
                return b;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 管理員專用：透過帳號與日期篩選預約紀錄
     */
    public List<Booking> findByUsernameAndDate(String username, String dateStr) {
        List<Booking> list = new ArrayList<>();
        // 修改處：使用 CAST(? AS DATE) 確保型別匹配
        String sql = "SELECT b.*, c.name as court_name, u.username " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.id " +
                "JOIN courts c ON b.court_id = c.id " +
                "WHERE u.username = ? AND DATE(b.start_time) = CAST(? AS DATE) " +
                "AND b.status != 'CANCELLED' " +
                "ORDER BY b.start_time ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, dateStr); // 傳入 "2026-05-11"

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("court_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getInt("total_fee")
                );
                b.setStatus(rs.getString("status"));
                b.setCourtName(rs.getString("court_name"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("❌ 透過帳號查詢預約失敗: " + e.getMessage());
        }
        return list;
    }
    /**
     * 執行預約單內容的實體更新 (管理員修改用)
     * @param bId 預約單 ID
     * @param courtId 新的球場 ID
     * @param start 新的開始時間
     * @param end 新的結束時間
     * @return 是否更新成功
     */
    public boolean updateBookingDetail(int bId, int courtId, LocalDateTime start, LocalDateTime end) {
        // SQL 語法：更新球場、開始時間、結束時間，並同步更新 timestamp
        String sql = "UPDATE bookings SET court_id = ?, start_time = ?, end_time = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courtId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            ps.setInt(4, bId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ 資料庫更新成功：預約單 #" + bId);
                return true;
            } else {
                System.out.println("⚠️ 找不到該預約單 ID，更新失敗。");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ 執行 updateBookingDetail 時發生 SQL 錯誤: " + e.getMessage());
            return false;
        }
    }
}
