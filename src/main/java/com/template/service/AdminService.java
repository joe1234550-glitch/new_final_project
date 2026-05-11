package main.java.com.template.service;


import main.java.com.template.dao.BookingDAO;
import main.java.com.template.dao.courtDAO;
import main.java.com.template.model.Booking;
import main.java.com.template.model.court;
import main.java.com.template.model.enums.CourtStatus;
import main.java.com.template.model.enums.CourtType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class AdminService {
    private final courtDAO courtDAO = new courtDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // ==================== 球場管理 ====================

    /** 建立新球場（含驗證） */
    public List<String> createCourt(String name, String type, String description,int price) {
        List<String> errors = new ArrayList<>();
        if (name == null || name.isBlank()) errors.add("球場名稱不能為空");
        if (price < 0) errors.add("時租價格不能為負數");

        if (errors.isEmpty()) {
            court newCourt = new court(name, CourtType.valueOf(type), description, price);
            if (!courtDAO.insert(newCourt)) {
                errors.add("資料庫寫入失敗");
            }
        }
        return errors;
    }

    /** 修改球場狀態 */
    public boolean updateCourtStatus(int id, int statusChoice) {
        CourtStatus status = (statusChoice == 1) ? CourtStatus.AVAILABLE : CourtStatus.MAINTENANCE;
        return courtDAO.updateStatus(id, status);
    }

    // ==================== 預約管理 ====================

    /** 取得全系統預約紀錄 */
    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    /** 核可或取消預約 */
    public boolean updateBooking(int bId, String courtIdStr, String startStr, String endStr) {
        try {
            // 先抓出舊的資料，如果輸入是空的就用舊的
            Booking oldBooking = bookingDAO.findById(bId);
            if (oldBooking == null) return false;

            int finalCourtId = courtIdStr.isEmpty() ? oldBooking.getCourtId() : Integer.parseInt(courtIdStr);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime finalStart = startStr.isEmpty() ? oldBooking.getStartTime() : LocalDateTime.parse(startStr, dtf);
            LocalDateTime finalEnd = endStr.isEmpty() ? oldBooking.getEndTime() : LocalDateTime.parse(endStr, dtf);

            // 執行更新
            return bookingDAO.updateBookingDetail(bId, finalCourtId, finalStart, finalEnd);
        } catch (Exception e) {
            return false;
        }
    }

    // 取消預約
    public boolean cancelBooking(int bId) {
        return bookingDAO.updatePaymentInfo(bId, "ADMIN_CANCEL", "CANCELLED");
    }
    public boolean updateCourtStatus(int id, CourtStatus status) {
        // 這裡可以加入額外的業務檢查，例如：
        // if (id <= 0) return false;

        return courtDAO.updateStatus(id, status);
    }
    //找尋從帳號看這段期間的有預定球場的
    public List<Booking> findUserBookingsByDate(String username, String dateStr) {
        if (username == null || username.isEmpty() || dateStr == null) {
            return new ArrayList<>();
        }
        return bookingDAO.findByUsernameAndDate(username, dateStr);
    }

}
