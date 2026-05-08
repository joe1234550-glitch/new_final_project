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
        System.out.println("\n--- 預約清單 ---");
        list.forEach(b -> System.out.printf("ID:%d | 用戶:%d | 狀態:%s%n",
                b.getId(), b.getUserId(), b.getStatus()));
    }
    private void handleBookingProcess() {
        System.out.print("請輸入欲處理的預約單 ID: ");
        int bId = Integer.parseInt(sc.nextLine());

        System.out.println("請選擇操作：[1] 核可支付(PAID) [2] 取消預約(CANCEL)");
        String choice = sc.nextLine();
        String action = choice.equals("1") ? "PAID" : "CANCEL";

        // 呼叫 Service 執行邏輯
        if (adminService.processBooking(bId, action)) {
            System.out.println("✅ 預約單狀態已成功更新。");
        } else {
            System.out.println("❌ 更新失敗，請檢查 ID 是否正確。");
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