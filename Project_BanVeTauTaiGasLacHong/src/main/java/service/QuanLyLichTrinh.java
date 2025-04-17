package service;

import dao.LichTrinhTauDAO;
import model.LichTrinhTau;
import model.TrangThai;

import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý các chức năng liên quan đến lịch trình tàu
 */
public class QuanLyLichTrinh {
    private static final Logger LOGGER = Logger.getLogger(QuanLyLichTrinh.class.getName());

    private final LichTrinhTauDAO lichTrinhTauDAO;
    private final AITravelTimePredictor aiPredictor;

    // Cache dự đoán thời gian đến (vì không lưu trong model)
    private final Map<String, LocalTime> estimatedArrivalCache = new HashMap<>();

    public QuanLyLichTrinh(LichTrinhTauDAO lichTrinhTauDAO) {
        this.lichTrinhTauDAO = lichTrinhTauDAO;
        this.aiPredictor = AITravelTimePredictor.getInstance();
    }

    /**
     * Dự đoán thời gian đến dựa trên AI cho một lịch trình cụ thể
     * @param maLich Mã lịch trình
     * @return Thông tin dự đoán thời gian đến, hoặc null nếu có lỗi
     */
    public PredictionInfo predictArrivalTime(String maLich) {
        try {
            // Lấy thông tin lịch trình
            LichTrinhTau lichTrinh = lichTrinhTauDAO.getById(maLich);
            if (lichTrinh == null) {
                LOGGER.warning("Không tìm thấy lịch trình với mã " + maLich);
                return null;
            }

            // Dùng AI để dự đoán thời gian di chuyển
            AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(lichTrinh);

            // Tính toán thời gian đến dự kiến
            LocalTime departureTime = lichTrinh.getGioDi();
            LocalTime estimatedArrival = prediction.getEstimatedArrivalTime(departureTime);

            // Lưu vào cache
            estimatedArrivalCache.put(maLich, estimatedArrival);

            // Định dạng giờ
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Tạo thông tin dự đoán
            PredictionInfo info = new PredictionInfo();
            info.setMaLich(maLich);
            info.setTenTau(lichTrinh.getTau().getTenTau());
            info.setTuyenDuong(lichTrinh.getTau().getTuyenTau().getGaDi() + " → " +
                    lichTrinh.getTau().getTuyenTau().getGaDen());
            info.setGioDi(departureTime.format(timeFormatter));
            info.setGioDenDuKien(estimatedArrival.format(timeFormatter));
            info.setThoiGianDiChuyen(prediction.getFormattedTravelTime());
            info.setDoChinhXac(prediction.getAccuracyPercentage());
            info.setGiaiThich(prediction.getExplanation());

            return info;

        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi dự đoán thời gian đến", e);
            return null;
        }
    }

    /**
     * Dự đoán thời gian đến cho tất cả các lịch trình chưa khởi hành
     * @return Danh sách thông tin dự đoán, hoặc null nếu có lỗi
     */
    public List<PredictionInfo> predictAllPendingSchedules() {
        try {
            // Lấy danh sách lịch trình chưa khởi hành
            List<LichTrinhTau> pendingSchedules = lichTrinhTauDAO.getListLichTrinhTauByTrangThai(TrangThai.CHUA_KHOI_HANH);

            // Dự đoán cho từng lịch trình
            return pendingSchedules.stream()
                    .map(schedule -> predictArrivalTime(schedule.getMaLich()))
                    .filter(prediction -> prediction != null)
                    .collect(java.util.stream.Collectors.toList());

        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi dự đoán thời gian cho các lịch trình", e);
            return null;
        }
    }

    /**
     * Lấy thời gian đến dự kiến đã được dự đoán cho một lịch trình
     * Nếu chưa có trong cache, thực hiện dự đoán mới
     * @param lichTrinh Đối tượng lịch trình
     * @return Thời gian đến dự kiến
     */
    public LocalTime getEstimatedArrivalTime(LichTrinhTau lichTrinh) {
        String maLich = lichTrinh.getMaLich();

        // Kiểm tra cache
        if (estimatedArrivalCache.containsKey(maLich)) {
            return estimatedArrivalCache.get(maLich);
        }

        // Nếu chưa có trong cache, dự đoán và lưu
        try {
            AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(lichTrinh);
            LocalTime estimatedArrival = prediction.getEstimatedArrivalTime(lichTrinh.getGioDi());

            // Lưu vào cache
            estimatedArrivalCache.put(maLich, estimatedArrival);

            return estimatedArrival;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể dự đoán thời gian đến, sử dụng cách tính đơn giản", e);

            // Sử dụng cách tính đơn giản nếu có lỗi
            int hours = estimateDefaultTravelTime(lichTrinh);
            return lichTrinh.getGioDi().plusHours(hours);
        }
    }

    /**
     * Ước tính thời gian di chuyển mặc định (giờ)
     */
    private int estimateDefaultTravelTime(LichTrinhTau lichTrinh) {
        try {
            String routeId = lichTrinh.getTau().getTuyenTau().getMaTuyen();

            if (routeId.contains("HN-SG") || routeId.contains("SG-HN")) {
                return 27; // 27 giờ cho tuyến Hà Nội - Sài Gòn
            } else if (routeId.contains("HN-DN") || routeId.contains("DN-HN")) {
                return 13; // 13 giờ cho tuyến Hà Nội - Đà Nẵng
            } else if (routeId.contains("DN-SG") || routeId.contains("SG-DN")) {
                return 16; // 16 giờ cho tuyến Đà Nẵng - Sài Gòn
            } else {
                return 8;  // Mặc định 8 giờ cho các tuyến khác
            }
        } catch (Exception e) {
            return 8; // Giá trị mặc định
        }
    }

    /**
     * Cập nhật cache thời gian đến dự kiến cho một lịch trình
     * @param maLich Mã lịch trình
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateEstimatedArrivalTime(String maLich) {
        try {
            // Lấy thông tin lịch trình
            LichTrinhTau lichTrinh = lichTrinhTauDAO.getById(maLich);
            if (lichTrinh == null) {
                LOGGER.warning("Không tìm thấy lịch trình với mã " + maLich);
                return false;
            }

            // Dùng AI để dự đoán thời gian di chuyển
            AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(lichTrinh);

            // Tính toán thời gian đến dự kiến
            LocalTime departureTime = lichTrinh.getGioDi();
            LocalTime estimatedArrival = prediction.getEstimatedArrivalTime(departureTime);

            // Cập nhật cache
            estimatedArrivalCache.put(maLich, estimatedArrival);

            return true;

        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật thời gian đến dự kiến", e);
            return false;
        }
    }

    /**
     * Cập nhật cache thời gian đến dự kiến cho tất cả các lịch trình
     * @return Số lượng lịch trình được cập nhật thành công
     */
    public int updateAllEstimatedArrivalTimes() {
        try {
            // Lấy tất cả lịch trình
            List<LichTrinhTau> allSchedules = lichTrinhTauDAO.getAllList();

            int updatedCount = 0;

            for (LichTrinhTau lichTrinh : allSchedules) {
                // Bỏ qua các lịch trình đã hủy
                if (lichTrinh.getTrangThai() == TrangThai.DA_HUY) {
                    continue;
                }

                try {
                    // Dùng AI để dự đoán thời gian di chuyển
                    AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(lichTrinh);

                    // Tính toán thời gian đến dự kiến
                    LocalTime departureTime = lichTrinh.getGioDi();
                    LocalTime estimatedArrival = prediction.getEstimatedArrivalTime(departureTime);

                    // Cập nhật cache
                    estimatedArrivalCache.put(lichTrinh.getMaLich(), estimatedArrival);

                    updatedCount++;
                } catch (Exception e) {
                    LOGGER.warning("Không thể dự đoán thời gian đến cho lịch trình " + lichTrinh.getMaLich());
                }
            }

            return updatedCount;

        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật thời gian đến dự kiến", e);
            return 0;
        }
    }

    /**
     * Xóa cache
     */
    public void clearCache() {
        estimatedArrivalCache.clear();
    }

    /**
     * Class chứa thông tin dự đoán
     */
    public static class PredictionInfo {
        private String maLich;
        private String tenTau;
        private String tuyenDuong;
        private String gioDi;
        private String gioDenDuKien;
        private String thoiGianDiChuyen;
        private int doChinhXac;
        private String giaiThich;

        // Getters và setters
        public String getMaLich() {
            return maLich;
        }

        public void setMaLich(String maLich) {
            this.maLich = maLich;
        }

        public String getTenTau() {
            return tenTau;
        }

        public void setTenTau(String tenTau) {
            this.tenTau = tenTau;
        }

        public String getTuyenDuong() {
            return tuyenDuong;
        }

        public void setTuyenDuong(String tuyenDuong) {
            this.tuyenDuong = tuyenDuong;
        }

        public String getGioDi() {
            return gioDi;
        }

        public void setGioDi(String gioDi) {
            this.gioDi = gioDi;
        }

        public String getGioDenDuKien() {
            return gioDenDuKien;
        }

        public void setGioDenDuKien(String gioDenDuKien) {
            this.gioDenDuKien = gioDenDuKien;
        }

        public String getThoiGianDiChuyen() {
            return thoiGianDiChuyen;
        }

        public void setThoiGianDiChuyen(String thoiGianDiChuyen) {
            this.thoiGianDiChuyen = thoiGianDiChuyen;
        }

        public int getDoChinhXac() {
            return doChinhXac;
        }

        public void setDoChinhXac(int doChinhXac) {
            this.doChinhXac = doChinhXac;
        }

        public String getGiaiThich() {
            return giaiThich;
        }

        public void setGiaiThich(String giaiThich) {
            this.giaiThich = giaiThich;
        }
    }
}