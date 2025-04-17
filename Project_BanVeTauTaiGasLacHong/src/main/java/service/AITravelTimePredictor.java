package service;

import model.LichTrinhTau;
import model.TuyenTau;
import model.Tau;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class xử lý dự đoán thời gian di chuyển thực tế cho các lịch trình tàu
 * Phiên bản đơn giản này sử dụng thuật toán dự đoán dựa trên các yếu tố cơ bản
 */
public class AITravelTimePredictor {
    private static final Logger LOGGER = Logger.getLogger(AITravelTimePredictor.class.getName());

    // Lưu cache dự đoán để tránh tính toán lặp lại
    private final Map<String, PredictionResult> predictionCache = new HashMap<>();

    // Singleton pattern
    private static AITravelTimePredictor instance;

    private AITravelTimePredictor() {
        // Khởi tạo private
    }

    public static synchronized AITravelTimePredictor getInstance() {
        if (instance == null) {
            instance = new AITravelTimePredictor();
        }
        return instance;
    }

    /**
     * Dự đoán thời gian di chuyển dựa trên các yếu tố
     * @param lichTrinh Lịch trình tàu cần dự đoán
     * @return Kết quả dự đoán thời gian di chuyển (phút)
     */
    public PredictionResult predictTravelTime(LichTrinhTau lichTrinh) {
        String cacheKey = createCacheKey(lichTrinh);

        // Kiểm tra cache
        if (predictionCache.containsKey(cacheKey)) {
            return predictionCache.get(cacheKey);
        }

        try {
            // Lấy thông tin cơ bản
            TuyenTau tuyenTau = lichTrinh.getTau().getTuyenTau();
            if (tuyenTau == null) {
                throw new IllegalArgumentException("Lịch trình không có thông tin tuyến tàu");
            }

            // 1. Tính thời gian cơ bản dựa trên khoảng cách tuyến đường
            int baseMinutes = estimateBaseTime(tuyenTau);

            // 2. Điều chỉnh theo ngày trong tuần
            double dayOfWeekFactor = getDayOfWeekFactor(lichTrinh.getNgayDi().getDayOfWeek());

            // 3. Điều chỉnh theo giờ trong ngày
            double timeOfDayFactor = getTimeOfDayFactor(lichTrinh.getGioDi());

            // 4. Điều chỉnh theo dữ liệu thời tiết (giả lập)
            double weatherFactor = simulateWeatherFactor(lichTrinh.getNgayDi());

            // 5. Điều chỉnh theo loại tàu
            double trainFactor = getTrainTypeFactor(lichTrinh.getTau());

            // Kết hợp các yếu tố
            double adjustedMinutes = baseMinutes * dayOfWeekFactor * timeOfDayFactor * weatherFactor * trainFactor;

            // Làm tròn kết quả
            int predictedMinutes = (int) Math.round(adjustedMinutes);

            // Thêm biến động ngẫu nhiên ±5% để mô phỏng các yếu tố không dự đoán được
            Random random = new Random();
            double randomVariation = 0.95 + (random.nextDouble() * 0.1); // 0.95 to 1.05
            int finalMinutes = (int) Math.round(predictedMinutes * randomVariation);

            // Thêm thông tin chính xác
            int accuracyPercentage = calculateAccuracyPercentage();

            // Tạo kết quả dự đoán
            PredictionResult result = new PredictionResult(
                    finalMinutes,
                    accuracyPercentage,
                    buildExplanation(baseMinutes, dayOfWeekFactor, timeOfDayFactor, weatherFactor, trainFactor)
            );

            // Lưu vào cache
            predictionCache.put(cacheKey, result);

            return result;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể dự đoán thời gian di chuyển: " + e.getMessage(), e);

            // Trả về dự đoán mặc định nếu có lỗi
            return new PredictionResult(
                    120, // 2 giờ là giá trị mặc định
                    60,  // Độ chính xác thấp
                    "Không thể dự đoán chính xác do thiếu dữ liệu"
            );
        }
    }

    /**
     * Ước tính thời gian cơ bản dựa trên tuyến đường
     * (Trong thực tế, bạn sẽ lấy dữ liệu này từ cơ sở dữ liệu)
     */
    private int estimateBaseTime(TuyenTau tuyenTau) {
        // Đây là một mô phỏng đơn giản, trong thực tế bạn sẽ lấy từ dữ liệu thực

        // Lấy ID tuyến
        String maTuyen = tuyenTau.getMaTuyen();
        String gaDi = tuyenTau.getGaDi();
        String gaDen = tuyenTau.getGaDen();

        // Map thời gian cơ bản cho các tuyến phổ biến
        Map<String, Integer> baseTimeMap = new HashMap<>();
        baseTimeMap.put("HN-SG", 1620); // 27h = 1620 phút cho Hà Nội - Sài Gòn
        baseTimeMap.put("SG-HN", 1620);
        baseTimeMap.put("HN-DN", 780);  // 13h = 780 phút cho Hà Nội - Đà Nẵng
        baseTimeMap.put("DN-HN", 780);
        baseTimeMap.put("DN-SG", 960);  // 16h = 960 phút cho Đà Nẵng - Sài Gòn
        baseTimeMap.put("SG-DN", 960);

        // Tạo key tìm kiếm
        String lookupKey = gaDi + "-" + gaDen;

        // Trả về thời gian từ map hoặc tính toán dựa trên trung bình tốc độ
        if (baseTimeMap.containsKey(lookupKey)) {
            return baseTimeMap.get(lookupKey);
        } else {
            // Giá trị mặc định: 500km với tốc độ trung bình 60km/h = 8.33 giờ = 500 phút
            return 500;
        }
    }

    /**
     * Lấy hệ số điều chỉnh dựa trên ngày trong tuần
     */
    private double getDayOfWeekFactor(DayOfWeek dayOfWeek) {
        // Cuối tuần thường có nhiều hành khách và có thể chậm hơn
        switch (dayOfWeek) {
            case FRIDAY:
                return 1.1; // Ngày thứ 6: Chậm hơn 10%
            case SATURDAY:
                return 1.15; // Thứ 7: Chậm hơn 15%
            case SUNDAY:
                return 1.2; // Chủ nhật: Chậm hơn 20%
            default:
                return 1.0; // Các ngày khác: Thời gian bình thường
        }
    }

    /**
     * Lấy hệ số điều chỉnh dựa trên thời gian trong ngày
     */
    private double getTimeOfDayFactor(LocalTime time) {
        int hour = time.getHour();

        // Giờ cao điểm thường chậm hơn
        if ((hour >= 7 && hour <= 9) || (hour >= 16 && hour <= 19)) {
            return 1.15; // Giờ cao điểm: Chậm hơn 15%
        } else if (hour >= 22 || hour <= 5) {
            return 0.9; // Ban đêm: Nhanh hơn 10%
        } else {
            return 1.0; // Các giờ khác: Thời gian bình thường
        }
    }

    /**
     * Mô phỏng hệ số thời tiết (trong thực tế sẽ sử dụng API dự báo thời tiết)
     */
    private double simulateWeatherFactor(LocalDate date) {
        // Mô phỏng dự báo thời tiết dựa trên mùa
        int month = date.getMonthValue();

        // Mùa mưa (5-10) có thể chậm hơn
        if (month >= 5 && month <= 10) {
            Random random = new Random();
            // 30% khả năng có mưa lớn, 70% khả năng thời tiết bình thường
            if (random.nextDouble() < 0.3) {
                return 1.2; // Mưa lớn: Chậm hơn 20%
            }
        }

        // Mùa đông (11-2) có thể có sương mù
        if (month >= 11 || month <= 2) {
            Random random = new Random();
            // 20% khả năng có sương mù
            if (random.nextDouble() < 0.2) {
                return 1.15; // Sương mù: Chậm hơn 15%
            }
        }

        return 1.0; // Thời tiết bình thường
    }

    /**
     * Lấy hệ số điều chỉnh dựa trên loại tàu
     */
    private double getTrainTypeFactor(Tau tau) {
        // Trong thực tế, bạn sẽ kiểm tra loại tàu, năm sản xuất, v.v.
        // Ví dụ đơn giản: Dựa trên mã tàu để phân loại
        String maTau = tau.getMaTau();

        if (maTau.contains("SE") || maTau.contains("TN")) {
            return 0.9; // Tàu tốc hành: Nhanh hơn 10%
        } else if (maTau.contains("LP") || maTau.contains("SNT")) {
            return 1.1; // Tàu chậm: Chậm hơn 10%
        } else {
            return 1.0; // Tàu thường
        }
    }

    /**
     * Tính toán phần trăm độ chính xác của dự đoán
     */
    private int calculateAccuracyPercentage() {
        // Trong mô hình thực tế, độ chính xác sẽ được tính toán dựa trên
        // so sánh dự đoán trước đây với kết quả thực tế
        // Ở đây chúng ta giả định độ chính xác từ 75-95%
        Random random = new Random();
        return 75 + random.nextInt(20);
    }

    /**
     * Tạo key cache duy nhất cho mỗi dự đoán
     */
    private String createCacheKey(LichTrinhTau lichTrinh) {
        return lichTrinh.getMaLich() + "_" +
                lichTrinh.getNgayDi() + "_" +
                lichTrinh.getGioDi();
    }

    /**
     * Xây dựng giải thích cho dự đoán
     */
    private String buildExplanation(int baseMinutes, double dayFactor, double timeFactor,
                                    double weatherFactor, double trainFactor) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("Dự đoán dựa trên:");
        explanation.append("\n- Thời gian cơ bản: ").append(formatMinutesToTime(baseMinutes));

        if (dayFactor > 1.0) {
            explanation.append("\n- Điều chỉnh ngày trong tuần: +").append(Math.round((dayFactor - 1.0) * 100)).append("% (cuối tuần)");
        }

        if (timeFactor > 1.0) {
            explanation.append("\n- Điều chỉnh giờ trong ngày: +").append(Math.round((timeFactor - 1.0) * 100)).append("% (giờ cao điểm)");
        } else if (timeFactor < 1.0) {
            explanation.append("\n- Điều chỉnh giờ trong ngày: -").append(Math.round((1.0 - timeFactor) * 100)).append("% (ban đêm)");
        }

        if (weatherFactor > 1.0) {
            explanation.append("\n- Điều chỉnh thời tiết: +").append(Math.round((weatherFactor - 1.0) * 100)).append("% (không thuận lợi)");
        }

        if (trainFactor < 1.0) {
            explanation.append("\n- Điều chỉnh loại tàu: -").append(Math.round((1.0 - trainFactor) * 100)).append("% (tàu tốc hành)");
        } else if (trainFactor > 1.0) {
            explanation.append("\n- Điều chỉnh loại tàu: +").append(Math.round((trainFactor - 1.0) * 100)).append("% (tàu chậm)");
        }

        return explanation.toString();
    }

    /**
     * Định dạng số phút thành giờ:phút
     */
    private String formatMinutesToTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%d giờ %d phút", hours, mins);
    }

    /**
     * Xóa cache khi cần thiết
     */
    public void clearCache() {
        predictionCache.clear();
        LOGGER.info("Đã xóa cache dự đoán");
    }

    /**
     * Class chứa kết quả dự đoán
     */
    public static class PredictionResult {
        private final int predictedMinutes;
        private final int accuracyPercentage;
        private final String explanation;

        public PredictionResult(int predictedMinutes, int accuracyPercentage, String explanation) {
            this.predictedMinutes = predictedMinutes;
            this.accuracyPercentage = accuracyPercentage;
            this.explanation = explanation;
        }

        public int getPredictedMinutes() {
            return predictedMinutes;
        }

        public int getAccuracyPercentage() {
            return accuracyPercentage;
        }

        public String getExplanation() {
            return explanation;
        }

        public LocalTime getEstimatedArrivalTime(LocalTime departureTime) {
            return departureTime.plusMinutes(predictedMinutes);
        }

        public String getFormattedTravelTime() {
            int hours = predictedMinutes / 60;
            int minutes = predictedMinutes % 60;

            if (hours > 0) {
                return String.format("%d giờ %d phút", hours, minutes);
            } else {
                return String.format("%d phút", minutes);
            }
        }
    }
}