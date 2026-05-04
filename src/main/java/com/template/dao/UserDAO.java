package main.java.com.template.dao;

import main.java.com.template.config.DatabaseConfig;
import main.java.com.template.model.User;
import java.sql.*;

/**
 * 使用者 DAO — 註冊、登入、查詢
 *
 * 📝 DAO 的職責：
 *    - 只負責「資料庫存取」（SQL 操作）
 *    - 不做業務邏輯判斷（那是 Service 的工作）
 *    - 每個 public 方法對應一種 SQL 操作
 */
public class UserDAO {

//    /**
//     * 註冊新使用者
//     * @return 新使用者的 ID，失敗回傳 -1
//     */
//    public int register(String username, String passwordHash) {
//        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'USER') RETURNING id";
//
//        try (Connection conn = DatabaseConfig.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setString(1, username);
//            ps.setString(2, passwordHash);
//
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("id");
//            }
//        } catch (SQLException e) {
//            // 帳號重複時 PostgreSQL 會拋出 unique violation
//            if (e.getMessage().contains("duplicate key")) {
//                System.out.println("❌ 帳號已存在！");
//            } else {
//                System.err.println("❌ 註冊失敗: " + e.getMessage());
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * 登入驗證
//     * @return 驗證成功回傳 User，失敗回傳 null
//     */
//    public User login(String username, String passwordHash) {
//        String sql = "SELECT id, username, role FROM users WHERE username = ? AND password_hash = ?";
//
//        try (Connection conn = DatabaseConfig.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setString(1, username);
//            ps.setString(2, passwordHash);
//
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                return new User(
//                    rs.getInt("id"),
//                    rs.getString("username"),
//                    rs.getString("role")
//                );
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ 登入查詢失敗: " + e.getMessage());
//        }
//        return null;
//    }
//
//    /** 依 ID 查詢使用者 */
//    public User findById(int id) {
//        String sql = "SELECT id, username, role FROM users WHERE id = ?";
//
//        try (Connection conn = DatabaseConfig.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setInt(1, id);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ 查詢使用者失敗: " + e.getMessage());
//        }
//        return null;
//    }
    /**
     * 註冊新使用者 (加入 phone 與 email)
     */
    public int register(String username, String passwordHash, String phone, String email) {
        // 配合你的類別，角色預設為 'USER'
        String sql = "INSERT INTO users (username, password_hash, role, phone, email) VALUES (?, ?, 'USER', ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, phone);
            ps.setString(4, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key") || e.getMessage().contains("Unique")) {
                System.out.println("❌ 帳號或 Email 已存在！");
            } else {
                System.err.println("❌ 註冊失敗: " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 登入驗證 (讀取完整欄位)
     */
    public User login(String username, String passwordHash) {
        String sql = "SELECT id, username, role, phone, email FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // 使用你定義的「從資料庫讀取」建構子，並透過 setter 補齊資訊
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                );
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ 登入查詢失敗: " + e.getMessage());
        }
        return null;
    }

    /** 依 ID 查詢使用者 */
    public User findById(int id) {
        String sql = "SELECT id, username, role, phone, email FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                );
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ 查詢使用者失敗: " + e.getMessage());
        }
        return null;
    }


    public boolean updateProfile(int id, String phone, String email) {

        String sql = "UPDATE users SET phone = ?, email = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);
            ps.setString(2, email);
            // 設定更新時間為現在
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(4, id);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // 如果有成功更新到資料，回傳 true
        } catch (SQLException e) {
            System.err.println("❌ UserDAO 更新資料失敗: " + e.getMessage());
            return false;
        }
    }
}
