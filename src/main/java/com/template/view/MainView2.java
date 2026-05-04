package main.java.com.template.view;
import main.java.com.template.model.User;
import main.java.com.template.model.court;
import main.java.com.template.model.Booking;
import main.java.com.template.model.enums.CourtStatus;
import main.java.com.template.model.enums.CourtType ;

import main.java.com.template.dao.UserDAO;
import main.java.com.template.dao.courtDAO;
import main.java.com.template.dao.BookingDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class MainView2 {

    private final Scanner scanner;
    private final UserDAO userDAO = new UserDAO();
    private final courtDAO courtDAO = new courtDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private User currentUser;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MainView2(Scanner scanner) {
        this.scanner = scanner;
    }

    // ==================== 啟動入口 ====================

    public void start() {
        printBanner();

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

        mainMenu();
    }

    // ==================== 身份驗證 ====================

    private void login() {
        System.out.print("帳號: ");
        String username = readLine();
        System.out.print("密碼: ");
        String password = readLine();

        String hash = Integer.toHexString(password.hashCode());
        currentUser = userDAO.login(username, hash);

        if (currentUser == null) {
            System.out.println("❌ 帳號或密碼錯誤！");
        } else {
            System.out.println("✅ 歡迎回來，" + currentUser.getUsername() + "！");
            // 檢查舊帳號是否缺失資料
            if (currentUser.getPhone() == null || currentUser.getEmail() == null) {
                completeProfile();
            }
        }
    }

    private void register() {
        System.out.print("帳號（3~30 字元）: ");
        String username = readLine();
        System.out.print("密碼: ");
        String password = readLine();
        System.out.print("電話: ");
        String phone = readLine();
        System.out.print("電子信箱: ");
        String email = readLine();

        String hash = Integer.toHexString(password.hashCode());
        int id = userDAO.register(username, hash, phone, email);

        if (id > 0) {
            System.out.println("✅ 註冊成功！請重新登入。");
        } else {
            System.out.println("❌ 註冊失敗，帳號或信箱可能已重複。");
        }
    }

    private void completeProfile() {
        System.out.println("⚠️ 您的資料不完整，請補填資訊：");
        System.out.print("電話: ");
        String phone = readLine();
        System.out.print("電子信箱: ");
        String email = readLine();
        if (userDAO.updateProfile(currentUser.getId(), phone, email)) {
            System.out.println("✅ 資料更新成功！");
        }
    }

    // ==================== 主選單 ====================

    private void mainMenu() {
        while (true) {
            System.out.println("\n══════════ 網球場預約系統 ══════════");
            System.out.println("[1] 🎾 瀏覽球場狀態");
            System.out.println("[2] 📅 預約球場");
            System.out.println("[3] 📜 我的預約紀錄");
            System.out.println("[4] 🛠️  管理員選單 (維修設定)");
            System.out.println("[0] 🚪 登出");
            System.out.print("> ");

            switch (readLine()) {
                case "1" -> listCourts();
                case "2" -> makeBooking();
                case "3" -> listMyBookings();
                case "4" -> adminMenu();
                case "0" -> {
                    System.out.println("👋 已登出");
                    currentUser = null;
                    return;
                }
                default -> System.out.println("❌ 無效選項");
            }
        }
    }

    // ==================== 球場與預約功能 ====================

    /** 1. 瀏覽所有球場 */
    private void listCourts() {
        List<court> courts = courtDAO.findAll();
        if (courts.isEmpty()) {
            System.out.println("\n📭 目前沒有球場資訊。");
            return;
        }
        System.out.println("\n── 所有球場狀態 ──");
        for (court c : courts) {
            System.out.println(c.display());
        }
    }

    /** 2. 進行預約 */
    private void makeBooking() {
        listCourts();
        System.out.print("\n請輸入欲預約的球場 ID: ");
        int courtId = readInt();

        court selectedCourt = courtDAO.findById(courtId);
        if (selectedCourt == null) {
            System.out.println("❌ 找不到該球場。");
            return;
        }
        if (selectedCourt.getStatus() != CourtStatus.AVAILABLE) {
            System.out.println("❌ 該球場目前無法預約 (已預約或維修中)。");
            return;
        }

        System.out.print("請輸入預約開始小時 (0-23): ");
        int startHour = readInt();

        // 建立預約時間 (假設預約當天)
        LocalDateTime startTime = LocalDateTime.now().withHour(startHour).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(1);

        System.out.printf("確認預約：%s | 時間：%s | 費用：$%d%n",
                selectedCourt.getName(), startTime.format(formatter), selectedCourt.getHourlyRate());
        System.out.print("確定預約？(y/n): ");

        if (readLine().equalsIgnoreCase("y")) {
            Booking b = new Booking(currentUser.getId(), courtId, startTime, endTime, selectedCourt.getHourlyRate());
            int bId = bookingDAO.create(b);

            if (bId > 0) {
                // 同步更新球場狀態
                courtDAO.updateStatus(courtId, CourtStatus.BOOKED);
                System.out.println("✅ 預約成功！單號：" + bId);
            } else {
                System.out.println("❌ 預約失敗，請洽櫃台。");
            }
        }
    }

    /** 3. 查看個人預約紀錄 */
    private void listMyBookings() {
        List<Booking> bookings = bookingDAO.findByUserId(currentUser.getId());
        if (bookings.isEmpty()) {
            System.out.println("\n📭 您目前沒有任何預約。");
            return;
        }
        System.out.println("\n── 我的預約紀錄 ──");
        for (Booking b : bookings) {
            System.out.printf("單號:[%d] | 球場:%s (%s) | 時間:%s | 狀態:%s | 費用:$%d%n",
                    b.getId(), b.getCourtName(), b.getCourtType(),
                    b.getStartTime().format(formatter), b.getStatus(), b.getTotalFee());
        }
    }

    /** 4. 管理員選單 (範例：維修場地) */
    private void adminMenu() {
        if (!"ADMIN".equals(currentUser.getRole())) {
            System.out.println("❌ 權限不足，僅限管理員進入。");
            return;
        }
        System.out.print("輸入欲維修的球場 ID: ");
        int cid = readInt();
        if (courtDAO.updateStatus(cid, CourtStatus.MAINTENANCE)) {
            System.out.println("✅ 球場已鎖定進行維修。");
        }
    }

    // ==================== 工具方法 ====================

    private void printBanner() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║       🎾 專業網球場預約系統 🎾        ║");
        System.out.println("║      Tennis Court Booking System       ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    private String readLine() {
        return scanner.nextLine().trim();
    }

    private int readInt() {
        try {
            return Integer.parseInt(readLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
