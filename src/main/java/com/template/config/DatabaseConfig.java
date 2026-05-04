package main.java.com.template.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 資料庫連線設定
 *
 * 📝 同學請修改以下三個常數：
 *    - URL：你的資料庫名稱
 *    - USER：你的帳號
 *    - PASSWORD：你的密碼
 */
public class DatabaseConfig {

    private static final String URL      = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "my_secret_password";

    // 載入 JDBC 驅動程式（只需執行一次）
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ 找不到 PostgreSQL JDBC Driver！");
            System.err.println("   請確認 lib/postgresql-42.7.3.jar 是否存在。");
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得一條新的資料庫連線
     * ⚠️ 用完記得 close()！建議搭配 try-with-resources 使用：
     *
     *   try (Connection conn = DatabaseConfig.getConnection()) {
     *       // ... 使用 conn ...
     *   }  // 自動 close
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 測試連線是否正常 */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
