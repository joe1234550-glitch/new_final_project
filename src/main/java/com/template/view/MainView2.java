package main.java.com.template.view;
import main.java.com.template.model.User;
import main.java.com.template.model.court;
import main.java.com.template.model.Booking;
import main.java.com.template.model.enums.CourtStatus;
import main.java.com.template.model.enums.CourtType ;

import main.java.com.template.dao.UserDAO;
import main.java.com.template.dao.courtDAO;
import main.java.com.template.dao.BookingDAO;

import java.time.LocalDate;
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

//    private void login() {
//        System.out.print("帳號: ");
//        String username = readLine();
//        System.out.print("密碼: ");
//        String password = readLine();
//
//        String hash = Integer.toHexString(password.hashCode());
//        currentUser = userDAO.login(username, hash);
//
//        if (currentUser == null) {
//            System.out.println("❌ 帳號或密碼錯誤！");
//        } else {
//            System.out.println("✅ 歡迎回來，" + currentUser.getUsername() + "！");
//            // 檢查舊帳號是否缺失資料
//            if (currentUser.getPhone() == null || currentUser.getEmail() == null) {
//                completeProfile();
//            }
//        }
//    }
private void login() {
    System.out.print("帳號: ");
    String username = readLine();
    System.out.print("密碼: ");
    String password = readLine();

    // 密碼加密處理
    String hash = Integer.toHexString(password.hashCode());
    currentUser = userDAO.login(username, hash);

    if (currentUser == null) {
        System.out.println("❌ 帳號或密碼錯誤！");
    } else {
        System.out.println("✅ 歡迎回來，" + currentUser.getUsername() + "！");

        // 1. 檢查資料完整性 (你原本的邏輯)
        if (currentUser.getPhone() == null || currentUser.getEmail() == null) {
            completeProfile();
        }

        // 2. 權限分流跳轉
        // 注意：這裡假設 User 類別中有 getRole() 方法，且回傳 "ADMIN" 或 "USER"
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            System.out.println("🚀 偵測到管理員權限，進入後台管理系統...");
            AdminView adminView = new AdminView(currentUser);
            adminView.showMenu(); // 進入管理員選單
        } else {
            // 一般用戶留在 MainView 的選單（通常在 login 呼叫完後會回到原本的迴圈，或在此呼叫 showMenu）
            // 如果你的 MainView 是在 Main 裡面呼叫，這裡可以不做動作直接 return
            System.out.println("🏠 進入使用者預約系統...");
            // 如果需要跳轉，可以在此呼叫 showMenu();
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

            System.out.println("[0] 🚪 登出");
            System.out.print("> ");

            switch (readLine()) {
                case "1" -> listCourts();
                case "2" -> makeBooking();
                case "3" -> listMyBookings();

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
        listCourts(); // 顯示所有球場
        System.out.print("\n請輸入欲預約的球場 ID: ");
        int courtId = readInt();

        court selectedCourt = courtDAO.findById(courtId);
        if (selectedCourt == null) {
            System.out.println("❌ 找不到該球場。");
            return;
        }

        // 這裡只檢查是否在「維修中」，不再檢查是否被 BOOKED
        if (selectedCourt.getStatus() == CourtStatus.MAINTENANCE) {
            System.out.println("❌ 該球場維修中，暫不開放預約。");
            return;
        }
        // 2. 日期查詢與空檔顯示
        System.out.print("請輸入欲預約日期 (格式 yyyy-MM-dd，直接 Enter 為今日): ");
        String dateInput = readLine();
        LocalDate targetDate;
        try {
            targetDate = dateInput.isBlank() ? LocalDate.now() : LocalDate.parse(dateInput);
            // 先顯示該日期的空檔，幫助使用者決定時間
            showAvailableSlots(courtId, targetDate);
        } catch (Exception e) {
            System.out.println("❌ 日期格式錯誤。");
            return;
        }
        // 3. 輸入開始與結束時間
        System.out.print("請輸入開始時間 (格式 yyyy-MM-dd HH:mm): ");
        String startStr = readLine();
        System.out.print("請輸入結束時間 (格式 yyyy-MM-dd HH:mm): ");
        String endStr = readLine();

        try {
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime startTime = LocalDateTime.parse(startStr, fullFormatter);
            LocalDateTime endTime = LocalDateTime.parse(endStr, fullFormatter);

            // 基本邏輯檢查
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                System.out.println("❌ 錯誤：結束時間必須晚於開始時間。");
                return;
            }

            // --- 核心改動：檢查該時段是否已被佔用 ---
            if (bookingDAO.isTimeSlotOccupied(courtId, startTime, endTime)) {
                System.out.println("❌ 抱歉，該時段已有人預約，請選擇其他時間。");
                return;
            }

            // 計算費用 (以小時為單位，不滿一小時以一小時計)
            long durationHours = java.time.Duration.between(startTime, endTime).toHours();
            if (durationHours == 0) durationHours = 1;
            int totalFee = (int) (durationHours * selectedCourt.getHourlyRate());

            System.out.printf("\n📋 預約確認 📋\n球場：%s\n時間：%s 至 %s\n總計費用：$%d%n",
                    selectedCourt.getName(), startStr, endStr, totalFee);
            System.out.print("確定預約？(y/n): ");

            if (readLine().equalsIgnoreCase("y")) {
                Booking b = new Booking(currentUser.getId(), courtId, startTime, endTime, totalFee);
                int bId = bookingDAO.create(b);

                if (bId > 0) {
                    // 注意：這裡【不要】再呼叫 courtDAO.updateStatus(courtId, CourtStatus.BOOKED)
                    // 因為我們現在允許多人在不同時段預約同一個球場。
                    System.out.println("✅ 預約成功！單號：" + bId);
                    paymentInterface(bId, totalFee);
                } else {
                    System.out.println("❌ 預約失敗，請洽櫃台。");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 時間格式錯誤，請確保格式為 2026-05-06 14:00");
        }
    }

    private void showAvailableSlots(int courtId, LocalDate date) {
        System.out.println("\n📅 " + date + " 球場開放狀況檢視：");
        List<Booking> bookings = bookingDAO.findByCourtAndDate(courtId, date);

        int openHour = 8;
        int closeHour = 22;

        for (int h = openHour; h < closeHour; h++) {
            LocalDateTime slotStart = date.atTime(h, 0);
            LocalDateTime slotEnd = date.atTime(h + 1, 0);

            boolean isOccupied = false;
            for (Booking b : bookings) {
                // 判斷該小時是否與現有任何預約重疊
                if (slotStart.isBefore(b.getEndTime()) && slotEnd.isAfter(b.getStartTime())) {
                    isOccupied = true;
                    break;
                }
            }

            String status = isOccupied ? "❌ 已被預約" : "✅ 可預約";
            System.out.printf("  %02d:00 - %02d:00  %s%n", h, h + 1, status);
        }
        System.out.println();
    }
// ==================== 支付介面實作 ====================

    /**
     * 支付處理介面
     * @param bookingId 預約單編號
     * @param fee 應付金額
     */
    private void paymentInterface(int bookingId, int fee) {
        System.out.println("\n💰 ────────── 結帳中心 ────────── 💰");
        System.out.println("   [ 預約編號 ] : " + bookingId);
        System.out.println("   [ 應付金額 ] : $" + fee);
        System.out.println("──────────────────────────────────");
        System.out.println("   請選擇支付方式：");
        System.out.println("   [1] 💳 線上支付 (信用卡/Apple Pay)");
        System.out.println("   [2] 💵 到場支付 (現金/現場掃碼)");
        System.out.println("   [0] 🕒 稍後再付 (回主選單)");
        System.out.print("\n選擇操作 > ");

        String choice = readLine();
        switch (choice) {
            case "1" -> {
                System.out.println("\n🔄 正在連線至金流閘道...");
                System.out.println("✅ [模擬] 刷卡成功！感謝您的預約。");
                // 更新資料庫狀態為已支付 (PAID)
                bookingDAO.updatePaymentInfo(bookingId, "ONLINE", "PAID");
            }
            case "2" -> {
                System.out.println("\n✅ 已選取現金支付。");
                System.out.println("📢 提醒：請於球賽開始前 15 分鐘至櫃台完成繳費。");
                // 更新資料庫為到場支付，狀態改為待付現 (PENDING_CASH)
                bookingDAO.updatePaymentInfo(bookingId, "CASH", "PENDING_CASH");
            }
            case "0" -> {
                System.out.println("\n⚠️ 您選擇稍後支付。");
                System.out.println("🕒 請注意，未完成支付的場地可能在 30 分鐘後自動取消。");
            }
            default -> System.out.println("\n❌ 輸入無效，視為暫緩支付。");
        }
        System.out.println("──────────────────────────────────\n");
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
