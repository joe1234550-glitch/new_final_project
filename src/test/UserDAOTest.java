package test;
import main.java.com.template.dao.UserDAO;
import main.java.com.template.model.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 照順序執行測試
class UserDAOTest {
    private UserDAO userDAO = new UserDAO();

    @Test
    @Order(1)
    @DisplayName("測試：註冊新用戶")
    void testRegister() {
        // Arrange
        String user = "testUser_" + System.currentTimeMillis(); // 避免重複
        String pass = "123456";
        String hash = Integer.toHexString(pass.hashCode());

        // Act
        int userId = userDAO.register(user, hash, "0912345678", "test@test.com");

        // Assert
        assertTrue(userId > 0, "註冊應該回傳大於 0 的 ID");
    }

    @Test
    @Order(2)
    @DisplayName("測試：登入成功邏輯")
    void testLoginSuccess() {
        // Arrange: 假設資料庫已有 admin 帳號
        String username = "admin";
        String password = "admin"; // 你的初始密碼
        String hash = Integer.toHexString(password.hashCode());

        // Act
        User user = userDAO.login(username, hash);

        // Assert
        assertNotNull(user, "登入成功不應回傳 null");
        assertEquals(username, user.getUsername(), "登入回傳的帳號應相同");
        assertNotNull(user.getRole(), "角色權限不應為空");
    }

    @Test
    @Order(3)
    @DisplayName("測試：登入失敗 (密碼錯誤)")
    void testLoginWrongPassword() {
        // Arrange
        String username = "admin";
        String wrongHash = Integer.toHexString("wrongPass".hashCode());

        // Act
        User user = userDAO.login(username, wrongHash);

        // Assert
        assertNull(user, "密碼錯誤應該回傳 null");
    }
}
