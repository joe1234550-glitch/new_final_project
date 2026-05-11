package main.java.com.template.view;

import main.java.com.template.model.Booking;
import main.java.com.template.model.User;
import main.java.com.template.service.AdminService;
import main.java.com.template.model.court;
import main.java.com.template.model.Booking;
import main.java.com.template.model.enums.CourtStatus;
import main.java.com.template.model.enums.CourtType ;

import main.java.com.template.dao.UserDAO;
import main.java.com.template.dao.courtDAO;
import main.java.com.template.dao.BookingDAO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class AdminView {
    private final AdminService adminService = new AdminService();
    private final Scanner sc = new Scanner(System.in);
    private User adminUser;
    public AdminView(User currentUser) {
        this.adminUser = currentUser;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n🛠️ [管理員後台]");
            System.out.println("1. 查看所有預約 | 2. 審核預約 | 3. 新增球場 |4.管理員選單 (維修設定)|0. 登出");
            System.out.print("請輸入選項 > ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> displayAllBookings();
                case "2" -> handleBookingProcess();
                case "3" -> handleAddCourt();
                case "4" -> adminMenu();
                case "0" -> { return; }
            }
        }
    }

    private void handleAddCourt() {
        System.out.print("輸入球場名: "); String name = sc.nextLine();
        System.out.print("場地類型:"); String type = sc.nextLine();
        System.out.print("時租價格: "); int price = Integer.parseInt(sc.nextLine());
        System.out.print("說明"); String description = sc.nextLine();
        // 呼叫 Service 處理邏輯
        List<String> errors = adminService.createCourt(name, type, description, price);

        if (errors.isEmpty()) {
            System.out.println("✅ 球場新增成功！");
        } else {
            errors.forEach(err -> System.out.println("❌ " + err));
        }
    }

    private void displayAllBookings() {
        List<Booking> list = adminService.getAllBookings();

        System.out.println("\n" + "═".repeat(85));
        System.out.println("                        🎾 預約總清單 🎾");
        System.out.println("─".repeat(85));

        // 定義標題列：將「用戶ID」換成「用戶帳號」
        // %-6s (ID), %-15s (用戶帳號), %-15s (場地), %-22s (預約時間), %-10s (狀態)
        System.out.printf("%-6s %-15s %-15s %-22s %-10s%n",
                "ID", "用戶帳號", "場地", "預約時間", "狀態");
        System.out.println("─".repeat(85));

        if (list.isEmpty()) {
            System.out.println("                 目前系統內尚無預約紀錄。");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Booking b : list) {
                String timeRange = b.getStartTime().format(formatter);

                // 輸出內容：使用 b.getUsername()
                System.out.printf("%-6d %-15s %-15s %-22s %-10s%n",
                        b.getId(),
                        b.getUsername(),   // 顯示帳號
                        b.getCourtName(),  // 顯示場地名
                        timeRange,
                        b.getStatus());
            }
        }
        System.out.println("═".repeat(85) + "\n");
    }
    private void handleBookingProcess() {
        System.out.println("\n--- 🛠️ 預約單變更/審核流程 ---");

        // 1. 輸入日期與帳號篩選
        System.out.print("請輸入日期 (yyyy-MM-dd): ");
        String dateStr = sc.nextLine();
        System.out.print("請輸入使用者帳號: ");
        String username = sc.nextLine();

        // 2. 顯示該用戶在該日期的所有預約，方便管理員選擇 ID
        // 這裡建議在 AdminService 新增一個查詢方法
        List<Booking> userBookings = adminService.findUserBookingsByDate(username, dateStr);

        if (userBookings.isEmpty()) {
            System.out.println("❌ 該用戶在當天沒有預約紀錄。");
            return;
        }

        System.out.println("\n查得預約紀錄如下：");
        for (Booking b : userBookings) {
            System.out.printf("ID:[%d] 場地:%s 時間:%s ~ %s 狀態:%s%n",
                    b.getId(), b.getCourtName(), b.getStartTime(), b.getEndTime(), b.getStatus());
        }

        // 3. 選擇要修改的 ID
        System.out.print("\n請輸入欲處理的預約單 ID: ");
        int bId = Integer.parseInt(sc.nextLine());

        // 4. 選擇操作
        System.out.println("請選擇操作：[1] 修改預約內容 (時間/球場) [2] 取消預約 (CANCEL)");
        String choice = sc.nextLine();

        if (choice.equals("2")) {
            if (adminService.cancelBooking(bId)) {
                System.out.println("✅ 預約已取消。");
            }
        } else if (choice.equals("1")) {
            // 修改邏輯
            System.out.print("請輸入新的球場 ID (按 Enter 不修改): ");
            String newCourtId = sc.nextLine();
            System.out.print("請輸入新日期時間 (yyyy-MM-dd HH:mm): ");
            String newStart = sc.nextLine();
            System.out.print("請輸入結束時間 (yyyy-MM-dd HH:mm): ");
            String newEnd = sc.nextLine();

            if (adminService.updateBooking(bId, newCourtId, newStart, newEnd)) {
                System.out.println("✅ 預約內容已成功更新。");
            } else {
                System.out.println("❌ 更新失敗，可能時段重疊或場地維修中。");
            }
        }
    }
    /** 4. 管理員選單 (範例：維修場地、改成可使用) */
    private void adminMenu() {
        System.out.print("請輸入球場 ID: ");
        int cid = readInt(); // 呼叫下方我們補齊的輔助方法

        System.out.println("請選擇球場狀態：[1] 開放預約 (AVAILABLE) [2] 進入維修 (MAINTENANCE)");
        System.out.print("請選擇 > ");
        int choice = readInt();

        CourtStatus targetStatus;
        if (choice == 1) {
            targetStatus = CourtStatus.AVAILABLE;
        } else if (choice == 2) {
            targetStatus = CourtStatus.MAINTENANCE;
        } else {
            System.out.println("❌ 無效的選擇。");
            return;
        }

        // 2. 改為呼叫 adminService 而非直接叫 courtDAO
        boolean isSuccess = adminService.updateCourtStatus(cid, targetStatus);

        // 3. 根據 Service 回傳結果顯示畫面
        if (isSuccess) {
            String statusName = (targetStatus == CourtStatus.AVAILABLE) ? "開放預約" : "維修中";
            System.out.println("✅ 成功：球場 ID [" + cid + "] 目前狀態為 [" + statusName + "]");
        } else {
            System.out.println("❌ 失敗：無法更新狀態，請確認球場 ID 是否正確。");
        }
    }

// --- 補齊缺失的方法，解決紅字 ---

    private int readInt() {
        try {
            // 使用 sc.nextLine() 配合 Integer.parseInt 可以避免 Scanner 殘留換行符的問題
            return Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            return -1; // 如果輸入不是數字，回傳 -1 觸發錯誤邏輯
        }
    }

}