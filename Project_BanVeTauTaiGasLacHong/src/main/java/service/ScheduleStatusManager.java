package service;

import dao.LichTrinhTauDAO;
import model.LichTrinhTau;
import model.TrangThai;

import javax.swing.*;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý việc tự động cập nhật trạng thái lịch trình tàu
 */
public class ScheduleStatusManager {
    private static final Logger LOGGER = Logger.getLogger(ScheduleStatusManager.class.getName());

    private final LichTrinhTauDAO lichTrinhTauDAO;
    private final ScheduledExecutorService scheduler;
    private static final int UPDATE_INTERVAL_MINUTES = 5; // Kiểm tra mỗi 5 phút

    // Tham chiếu đến component cần làm mới
    private final Runnable refreshCallback;

    /**
     * Khởi tạo ScheduleStatusManager
     * @param lichTrinhTauDAO DAO để tương tác với dữ liệu lịch trình
     * @param refreshCallback Callback để làm mới giao diện sau khi cập nhật
     */
    public ScheduleStatusManager(LichTrinhTauDAO lichTrinhTauDAO, Runnable refreshCallback) {
        this.lichTrinhTauDAO = lichTrinhTauDAO;
        this.refreshCallback = refreshCallback;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // Bắt đầu lập lịch kiểm tra định kỳ
        startScheduledUpdates();
    }

    /**
     * Bắt đầu lịch kiểm tra định kỳ
     */
    private void startScheduledUpdates() {
        scheduler.scheduleAtFixedRate(
                this::updateDepartedSchedules,
                0,                   // Initial delay (start immediately)
                UPDATE_INTERVAL_MINUTES, // Period
                TimeUnit.MINUTES);
    }

    /**
     * Cập nhật trạng thái các lịch trình đã qua thời gian khởi hành
     */
    public void updateDepartedSchedules() {
        if (lichTrinhTauDAO == null) {
            LOGGER.warning("Không thể cập nhật trạng thái lịch trình do không có kết nối DAO");
            return;
        }

        try {
            // Lấy thời gian hiện tại
            LocalDateTime now = LocalDateTime.now();

            // Lấy danh sách lịch trình chưa khởi hành
            List<LichTrinhTau> pendingSchedules = lichTrinhTauDAO.getListLichTrinhTauByTrangThai(TrangThai.CHUA_KHOI_HANH, TrangThai.HOAT_DONG);

            if (pendingSchedules == null || pendingSchedules.isEmpty()) {
                LOGGER.info("Không có lịch trình nào cần cập nhật trạng thái");
                return;
            }

            LOGGER.info("Đang kiểm tra " + pendingSchedules.size() + " lịch trình chưa khởi hành hoặc đang hoạt động");

            int totalUpdated = 0;

            for (LichTrinhTau schedule : pendingSchedules) {
                // Gọi phương thức checkAndUpdateSingleSchedule
                boolean updated = checkAndUpdateSingleSchedule(schedule.getMaLich());
                if (updated) {
                    totalUpdated++;
                }
            }

            if (totalUpdated > 0) {
                LOGGER.info("Đã cập nhật tổng cộng " + totalUpdated + " lịch trình");

                // Gọi callback để làm mới giao diện
                if (refreshCallback != null) {
                    SwingUtilities.invokeLater(refreshCallback);
                }
            }

        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật trạng thái lịch trình", e);
        }
    }

    /**
     * Dừng các tác vụ định kỳ và giải phóng tài nguyên
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            LOGGER.info("Đang dừng trình quản lý cập nhật trạng thái lịch trình");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    LOGGER.warning("Buộc dừng scheduler do không thể đợi các tác vụ hoàn thành");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                LOGGER.warning("Bị ngắt khi đang dừng scheduler");
            }
        }
    }

    /**
     * Kiểm tra và cập nhật trạng thái một lịch trình cụ thể
     * @param scheduleId ID của lịch trình cần kiểm tra
     * @return true nếu đã cập nhật, false nếu không
     */
    public boolean checkAndUpdateSingleSchedule(String scheduleId) {
        if (lichTrinhTauDAO == null || scheduleId == null || scheduleId.isEmpty()) {
            return false;
        }

        try {
            // Lấy lịch trình từ CSDL
            LichTrinhTau schedule = lichTrinhTauDAO.getById(scheduleId);

            if (schedule == null) {
                LOGGER.warning("Không tìm thấy lịch trình với ID " + scheduleId);
                return false;
            }

            // Nếu lịch trình đã ở trạng thái đã khởi hành hoặc đã hủy, không cần cập nhật
            if (schedule.getTrangThai() == TrangThai.DA_KHOI_HANH ||
                    schedule.getTrangThai() == TrangThai.DA_HUY) {
                return false;
            }

            // Kiểm tra thời gian
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime departureTime = LocalDateTime.of(schedule.getNgayDi(), schedule.getGioDi());

            // Tính toán thời gian còn lại đến lúc khởi hành (tính bằng phút)
            long minutesToDeparture = java.time.Duration.between(now, departureTime).toMinutes();

            TrangThai newStatus = null;

            // Nếu thời gian khởi hành đã qua
            if (departureTime.isBefore(now)) {
                newStatus = TrangThai.DA_KHOI_HANH;
            }
            // Nếu thời gian khởi hành còn từ 0 đến 30 phút
            else if (minutesToDeparture <= 30 && minutesToDeparture > 0 &&
                    schedule.getTrangThai() != TrangThai.HOAT_DONG) {
                newStatus = TrangThai.HOAT_DONG;
            }

            // Nếu cần cập nhật trạng thái
            if (newStatus != null) {
                // Cập nhật trạng thái
                schedule.setTrangThai(newStatus);

                // Lưu vào CSDL
                boolean updated = lichTrinhTauDAO.update(schedule);
                if (updated) {
                    LOGGER.info("Đã cập nhật thành công lịch trình " + scheduleId +
                            " thành " + newStatus.getValue());
                    return true;
                } else {
                    LOGGER.warning("Không thể cập nhật lịch trình " + scheduleId);
                }
            }

            return false;
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật trạng thái lịch trình " + scheduleId, e);
            return false;
        }
    }
}