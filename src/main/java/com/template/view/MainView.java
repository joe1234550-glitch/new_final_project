package com.template.view;

import com.template.dao.UserDAO;
import com.template.model.Item;
import com.template.model.User;
import com.template.model.enums.Category;
import com.template.service.ItemService;
import java.util.List;
import java.util.Scanner;

/**
 * 主畫面 — CLI 選單與使用者互動
 *
 * 📝 View 的職責：
 *    - 顯示選單（System.out.println）
 *    - 接收輸入（Scanner）
 *    - 呼叫 Service 處理業務邏輯
 *    - 顯示結果
 *
 * 📝 View 不直接碰 SQL 或 DAO！
 */
public class MainView {

    private final Scanner scanner;
    private final UserDAO userDAO = new UserDAO();
    private final ItemService itemService = new ItemService();
    private User currentUser;

    public MainView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ==================== 啟動 ====================

    /** 應用程式入口 */
    public void start() {
        printBanner();

        // 登入/註冊迴圈
        while (currentUser == null) {
            System.out.println("\n[1] 登入  [2] 註冊  [0] 離開");
            System.out.print("> ");
            switch (readLine()) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> { System.out.println("👋 再見！"); return; }
                default -> System.out.println("❌ 無效選項");
            }
        }

        // 進入主選單
        mainMenu();
    }

    // ==================== 登入/註冊 ====================

    private void login() {
        System.out.print("帳號: ");
        String username = readLine();
        System.out.print("密碼: ");
        String password = readLine();

        // 簡易密碼 hash（教學用，正式環境請用 BCrypt）
        String hash = Integer.toHexString(password.hashCode());

        currentUser = userDAO.login(username, hash);
        if (currentUser == null) {
            System.out.println("❌ 帳號或密碼錯誤！");
        } else {
            System.out.println("✅ 歡迎回來，" + currentUser.getUsername() + "！");
        }
    }

    private void register() {
        System.out.print("帳號（3~30 字元）: ");
        String username = readLine();
        if (username.length() < 3 || username.length() > 30) {
            System.out.println("❌ 帳號長度必須在 3~30 字元之間");
            return;
        }
        System.out.print("密碼: ");
        String password = readLine();
        if (password.length() < 4) {
            System.out.println("❌ 密碼至少 4 個字元");
            return;
        }
        // --- 新增：讀取電話與信箱 ---
        System.out.print("電話: ");
        String phone = readLine();
        System.out.print("電子信箱: ");
        String email = readLine();

        String hash = Integer.toHexString(password.hashCode());
//      int id = userDAO.register(username, hash);
        int id = userDAO.register(username, hash, phone, email);
        if (id > 0) {
            System.out.println("✅ 註冊成功！請重新登入。");
        }
        else {
            System.out.println("❌ 註冊失敗，帳號或信箱可能已重複。");
        }
    }

    // ==================== 主選單 ====================

    private void mainMenu() {
        while (true) {
            System.out.println("\n═══════════ 主選單 ═══════════");
            System.out.println("[1] 📋 查看所有項目");
            System.out.println("[2] ➕ 新增項目");
            System.out.println("[3] 🔍 依類別篩選");
            System.out.println("[4] ✏️  編輯項目");
            System.out.println("[5] 🗑️  刪除項目");
            System.out.println("[6] 📊 統計資料");
            System.out.println("[0] 🚪 登出");
            System.out.print("> ");

            switch (readLine()) {
                case "1" -> listItems();
                case "2" -> addItem();
                case "3" -> filterByCategory();
                case "4" -> editItem();
                case "5" -> deleteItem();
                case "6" -> showStats();
                case "0" -> {
                    System.out.println("👋 已登出");
                    currentUser = null;
                    return;
                }
                default -> System.out.println("❌ 無效選項");
            }
        }
    }

    // ==================== 功能實作 ====================

    /** 查看所有項目 */
    private void listItems() {
        List<Item> items = itemService.getMyItems(currentUser.getId());
        if (items.isEmpty()) {
            System.out.println("\n📭 還沒有任何項目，快去新增一個吧！");
            return;
        }
        System.out.println("\n── 我的項目（共 " + items.size() + " 筆）──");
        for (Item item : items) {
            System.out.println(item.listDisplay());
        }
    }

    /** 新增項目 */
    private void addItem() {
        System.out.println("\n── 新增項目 ──");

        System.out.print("名稱: ");
        String name = readLine();

        System.out.println("類別：");
        for (Category c : Category.values()) {
            System.out.printf("  %s %s (%s)%n", c.getIcon(), c.getDisplayName(), c.name());
        }
        System.out.print("輸入類別代碼: ");
        String category = readLine();

        System.out.print("說明（可留空）: ");
        String desc = readLine();
        if (desc.isBlank()) desc = null;

        System.out.print("優先度（1~5）: ");
        int priority = readInt();

        List<String> errors = itemService.createItem(name, category, desc, priority, currentUser.getId());
        if (errors.isEmpty()) {
            System.out.println("✅ 新增成功！");
        } else {
            System.out.println("❌ 新增失敗：");
            errors.forEach(e -> System.out.println("   - " + e));
        }
    }

    /** 依類別篩選 */
    private void filterByCategory() {
        System.out.println("選擇類別：");
        for (Category c : Category.values()) {
            System.out.printf("  %s %s (%s)%n", c.getIcon(), c.getDisplayName(), c.name());
        }
        System.out.print("輸入類別代碼: ");
        String category = readLine().toUpperCase();

        List<Item> items = itemService.getByCategory(currentUser.getId(), category);
        if (items.isEmpty()) {
            System.out.println("📭 該類別沒有項目");
        } else {
            System.out.println("\n── " + category + "（共 " + items.size() + " 筆）──");
            items.forEach(i -> System.out.println(i.listDisplay()));
        }
    }

    /** 編輯項目 */
    private void editItem() {
        System.out.print("輸入項目 ID: ");
        int id = readInt();

        Item item = itemService.getById(id, currentUser.getId());
        if (item == null) {
            System.out.println("❌ 找不到該項目或沒有權限");
            return;
        }

        System.out.println("目前內容：" + item.display());
        System.out.print("新名稱（按 Enter 跳過）: ");
        String name = readLine();
        if (!name.isBlank()) item.setName(name);

        System.out.print("新說明（按 Enter 跳過）: ");
        String desc = readLine();
        if (!desc.isBlank()) item.setDescription(desc);

        System.out.print("新優先度 1~5（按 Enter 跳過）: ");
        String priStr = readLine();
        if (!priStr.isBlank()) {
            try { item.setPriority(Integer.parseInt(priStr)); }
            catch (NumberFormatException e) { /* 忽略 */ }
        }

        if (itemService.updateItem(item, currentUser.getId())) {
            System.out.println("✅ 更新成功！");
        } else {
            System.out.println("❌ 更新失敗");
        }
    }

    /** 刪除項目 */
    private void deleteItem() {
        System.out.print("輸入要刪除的項目 ID: ");
        int id = readInt();

        System.out.print("確定要刪除嗎？(y/n): ");
        if (!"y".equalsIgnoreCase(readLine())) {
            System.out.println("已取消");
            return;
        }

        if (itemService.deleteItem(id, currentUser.getId())) {
            System.out.println("✅ 已刪除");
        } else {
            System.out.println("❌ 刪除失敗（找不到或沒權限）");
        }
    }

    /** 統計資料 */
    private void showStats() {
        System.out.println("\n── 📊 統計 ──");
        String stats = itemService.getStats(currentUser.getId());
        if (stats.isBlank()) {
            System.out.println("  還沒有資料");
        } else {
            System.out.print(stats);
        }
    }

    // ==================== 工具方法 ====================

    private void printBanner() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║       📋 期末專題模板系統 📋          ║");
        System.out.println("║     請把這裡換成你的專題名稱          ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    /** 讀取一行輸入（去除前後空白） */
    private String readLine() {
        return scanner.nextLine().trim();
    }

    /** 讀取整數（輸入錯誤回傳 -1） */
    private int readInt() {
        try {
            return Integer.parseInt(readLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
