package service;

import dao.LichTrinhTauDAO;
import model.LichTrinhTau;
import model.Tau;
import model.TrangThai;
import model.TuyenTau;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Engine xử lý tin nhắn và trả lời cho chatbot
 */
public class ChatbotEngine {
    private static final Logger LOGGER = Logger.getLogger(ChatbotEngine.class.getName());

    private final LichTrinhTauDAO lichTrinhTauDAO;
    private final AITravelTimePredictor aiPredictor;

    // Từ khóa để xác định loại tin nhắn
    private final String[] SCHEDULE_KEYWORDS = {"lịch trình", "lịch tàu", "chuyến tàu", "khi nào", "mấy giờ", "ngày nào"};
    private final String[] STATUS_KEYWORDS = {"trạng thái", "tình trạng", "đã chạy", "đã khởi hành", "còn chạy"};
    private final String[] STATION_KEYWORDS = {"ga", "nhà ga", "điểm đến", "điểm khởi hành"};
    private final String[] TRAIN_KEYWORDS = {"tàu", "loại tàu", "tàu gì", "tàu nào"};
    private final String[] GREETING_KEYWORDS = {"xin chào", "chào", "hello", "hi", "hey"};
    private final String[] THANKS_KEYWORDS = {"cảm ơn", "thanks", "thank", "cám ơn"};
    private final String[] GOODBYE_KEYWORDS = {"tạm biệt", "bye", "goodbye", "gặp lại sau"};

    public ChatbotEngine(LichTrinhTauDAO lichTrinhTauDAO) {
        this.lichTrinhTauDAO = lichTrinhTauDAO;
        this.aiPredictor = AITravelTimePredictor.getInstance();
    }

    /**
     * Xử lý tin nhắn từ người dùng và trả về câu trả lời
     * @param message Tin nhắn từ người dùng
     * @return Câu trả lời
     */
    public String processMessage(String message) {
        // Chuyển đổi tin nhắn về chữ thường để dễ dàng so sánh
        String lowercaseMessage = message.toLowerCase();

        try {
            // Kiểm tra loại tin nhắn
            if (containsAny(lowercaseMessage, GREETING_KEYWORDS)) {
                return handleGreeting(message);
            } else if (containsAny(lowercaseMessage, THANKS_KEYWORDS)) {
                return handleThanks();
            } else if (containsAny(lowercaseMessage, GOODBYE_KEYWORDS)) {
                return handleGoodbye();
            } else if (containsAny(lowercaseMessage, SCHEDULE_KEYWORDS)) {
                return handleScheduleQuery(message);
            } else if (containsAny(lowercaseMessage, STATUS_KEYWORDS)) {
                return handleStatusQuery(message);
            } else if (containsAny(lowercaseMessage, STATION_KEYWORDS)) {
                return handleStationQuery(message);
            } else if (containsAny(lowercaseMessage, TRAIN_KEYWORDS)) {
                return handleTrainQuery(message);
            } else {
                return handleUnknownQuery();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý tin nhắn: " + e.getMessage(), e);
            return "Xin lỗi, đã xảy ra lỗi khi xử lý tin nhắn của bạn. Vui lòng thử lại với câu hỏi khác.";
        }
    }

    /**
     * Kiểm tra xem chuỗi có chứa bất kỳ từ khóa nào không
     */
    private boolean containsAny(String message, String[] keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Xử lý tin nhắn chào hỏi
     */
    private String handleGreeting(String message) {
        String[] greetings = {
                "Xin chào! Tôi có thể giúp gì cho bạn về lịch trình tàu hỏa?",
                "Chào bạn! Bạn cần tìm thông tin gì về lịch trình tàu?",
                "Xin chào! Tôi là trợ lý ảo. Bạn cần hỗ trợ thông tin gì về tàu hỏa?"
        };

        return getRandomResponse(greetings);
    }

    /**
     * Xử lý tin nhắn cảm ơn
     */
    private String handleThanks() {
        String[] responses = {
                "Không có gì! Rất vui khi được giúp bạn.",
                "Không có chi! Bạn cần hỗ trợ gì thêm không?",
                "Rất vui khi được hỗ trợ bạn. Bạn còn câu hỏi nào khác không?",
                "Đó là nhiệm vụ của tôi. Có gì cần hỗ trợ thêm, hãy cho tôi biết nhé!"
        };

        return getRandomResponse(responses);
    }

    /**
     * Xử lý tin nhắn tạm biệt
     */
    private String handleGoodbye() {
        String[] responses = {
                "Tạm biệt! Rất vui được hỗ trợ bạn.",
                "Chúc bạn một ngày tốt lành! Hẹn gặp lại.",
                "Tạm biệt và hẹn gặp lại. Khi cần thông tin về lịch trình tàu, hãy quay lại nhé!"
        };

        return getRandomResponse(responses);
    }

    /**
     * Xử lý truy vấn về lịch trình
     */
    private String handleScheduleQuery(String message) {
        try {
            // Tìm kiếm thông tin địa điểm
            String gaDi = extractLocation(message, "từ", "đi từ", "khởi hành từ");
            String gaDen = extractLocation(message, "đến", "tới", "về");

            // Tìm kiếm thông tin ngày
            LocalDate ngayDi = extractDate(message);

            // Nếu không tìm thấy đủ thông tin
            if (gaDi == null && gaDen == null) {
                return "Bạn có thể cho tôi biết ga đi và ga đến mà bạn muốn tìm lịch trình không? " +
                        "Ví dụ: 'Lịch trình tàu từ Hà Nội đến Đà Nẵng ngày mai'";
            }

            // Tìm kiếm lịch trình theo thông tin đã có
            List<LichTrinhTau> lichTrinhs = searchSchedules(gaDi, gaDen, ngayDi);

            if (lichTrinhs.isEmpty()) {
                String response = "Rất tiếc, tôi không tìm thấy lịch trình tàu nào ";

                if (gaDi != null) response += "từ " + gaDi + " ";
                if (gaDen != null) response += "đến " + gaDen + " ";
                if (ngayDi != null) response += "vào ngày " + ngayDi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " ";

                response += ".\n\nBạn có thể thử tìm với ga khác hoặc ngày khác không?";

                return response;
            } else {
                // Giới hạn số lượng kết quả hiển thị
                int displayCount = Math.min(lichTrinhs.size(), 5);

                StringBuilder response = new StringBuilder("Tôi đã tìm thấy ");
                response.append(lichTrinhs.size()).append(" lịch trình");

                if (gaDi != null) response.append(" từ ").append(gaDi);
                if (gaDen != null) response.append(" đến ").append(gaDen);
                if (ngayDi != null) response.append(" vào ngày ").append(ngayDi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                response.append(":\n\n");

                // Hiển thị thông tin các lịch trình
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                for (int i = 0; i < displayCount; i++) {
                    LichTrinhTau lichTrinh = lichTrinhs.get(i);
                    response.append(i + 1).append(". Mã lịch: ").append(lichTrinh.getMaLich())
                            .append(" - Tàu: ").append(lichTrinh.getTau().getMaTau())
                            .append(" - Giờ đi: ").append(lichTrinh.getGioDi().format(timeFormatter));

                    // Thêm thời gian đến dự kiến bằng AI
                    try {
                        AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(lichTrinh);
                        response.append(" - Giờ đến dự kiến: ")
                                .append(prediction.getEstimatedArrivalTime(lichTrinh.getGioDi()).format(timeFormatter));
                    } catch (Exception e) {
                        response.append(" - Giờ đến: Đang cập nhật");
                    }

                    response.append("\n");
                }

                // Thêm thông báo nếu còn nhiều kết quả khác
                if (lichTrinhs.size() > displayCount) {
                    response.append("\nVà ").append(lichTrinhs.size() - displayCount).append(" lịch trình khác. ")
                            .append("Bạn có muốn xem thêm không?");
                }

                return response.toString();
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn lịch trình: " + e.getMessage(), e);
            return "Xin lỗi, tôi không thể tìm kiếm lịch trình tàu vào lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Tìm kiếm lịch trình dựa trên thông tin đã có
     */
    private List<LichTrinhTau> searchSchedules(String gaDi, String gaDen, LocalDate ngayDi) {
        try {
            List<LichTrinhTau> allSchedules = lichTrinhTauDAO.getAllList();
            List<LichTrinhTau> filteredSchedules = new ArrayList<>();

            for (LichTrinhTau lichTrinh : allSchedules) {
                TuyenTau tuyenTau = lichTrinh.getTau().getTuyenTau();

                // Lọc theo ga đi
                if (gaDi != null && !containsIgnoreCase(tuyenTau.getGaDi(), gaDi)) {
                    continue;
                }

                // Lọc theo ga đến
                if (gaDen != null && !containsIgnoreCase(tuyenTau.getGaDen(), gaDen)) {
                    continue;
                }

                // Lọc theo ngày đi
                if (ngayDi != null && !lichTrinh.getNgayDi().equals(ngayDi)) {
                    continue;
                }

                // Bỏ qua các lịch trình đã hủy
                if (lichTrinh.getTrangThai() == TrangThai.DA_HUY) {
                    continue;
                }

                filteredSchedules.add(lichTrinh);
            }

            return filteredSchedules;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi khi tìm kiếm lịch trình: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Kiểm tra chuỗi có chứa chuỗi con không (không phân biệt hoa thường)
     */
    private boolean containsIgnoreCase(String source, String substring) {
        return source.toLowerCase().contains(substring.toLowerCase());
    }

    /**
     * Trích xuất địa điểm từ tin nhắn
     */
    private String extractLocation(String message, String... keywords) {
        message = message.toLowerCase();

        // Danh sách các ga tàu phổ biến
        String[] popularStations = {"hà nội", "sài gòn", "đà nẵng", "huế", "nha trang", "vinh", "đồng hới", "thanh hóa", "hải phòng"};

        // Tìm kiếm vị trí của các từ khóa
        int keywordIndex = -1;
        String foundKeyword = null;

        for (String keyword : keywords) {
            int index = message.indexOf(keyword);
            if (index != -1 && (keywordIndex == -1 || index < keywordIndex)) {
                keywordIndex = index;
                foundKeyword = keyword;
            }
        }

        if (keywordIndex == -1) {
            // Nếu không tìm thấy từ khóa, tìm kiếm các ga phổ biến
            for (String station : popularStations) {
                if (message.contains(station)) {
                    return capitalizeWords(station);
                }
            }
            return null;
        }

        // Tìm địa điểm sau từ khóa
        int startIndex = keywordIndex + foundKeyword.length();
        String remainingText = message.substring(startIndex).trim();

        // Tìm đến khi gặp dấu câu hoặc từ khóa khác
        int endIndex = remainingText.length();
        for (String stopWord : new String[]{"và", "đến", "từ", "tới", "ngày", "lúc", "giờ", "?"}) {
            int idx = remainingText.indexOf(stopWord);
            if (idx != -1 && idx < endIndex) {
                endIndex = idx;
            }
        }

        String location = remainingText.substring(0, endIndex).trim();

        // Kiểm tra xem chuỗi có quá ngắn không
        if (location.length() < 2) {
            return null;
        }

        return capitalizeWords(location);
    }

    /**
     * Viết hoa chữ cái đầu mỗi từ
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        String[] words = text.split("\\s");

        for (String word : words) {
            if (word.length() > 0) {
                result.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Trích xuất ngày từ tin nhắn
     */
    private LocalDate extractDate(String message) {
        message = message.toLowerCase();

        // Kiểm tra các từ ngày tương đối
        if (message.contains("hôm nay")) {
            return LocalDate.now();
        } else if (message.contains("ngày mai") || message.contains("mai")) {
            return LocalDate.now().plusDays(1);
        } else if (message.contains("ngày kia")) {
            return LocalDate.now().plusDays(2);
        }

        // Tìm kiếm ngày theo định dạng dd/MM hoặc dd-MM
        Pattern datePattern = Pattern.compile("(\\d{1,2})[-/](\\d{1,2})(?:[-/](\\d{4}|\\d{2}))?");
        Matcher matcher = datePattern.matcher(message);

        if (matcher.find()) {
            try {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = LocalDate.now().getYear();

                // Nếu có năm
                if (matcher.group(3) != null) {
                    String yearStr = matcher.group(3);
                    if (yearStr.length() == 2) {
                        year = 2000 + Integer.parseInt(yearStr);
                    } else {
                        year = Integer.parseInt(yearStr);
                    }
                }

                return LocalDate.of(year, month, day);
            } catch (DateTimeParseException | NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Không thể chuyển đổi ngày: " + e.getMessage(), e);
            }
        }

        return null;
    }

    /**
     * Xử lý truy vấn về trạng thái
     */
    private String handleStatusQuery(String message) {
        try {
            // Tìm ID lịch trình hoặc mã tàu trong tin nhắn
            String id = extractId(message);

            if (id == null) {
                return "Bạn có thể cho tôi biết mã lịch trình hoặc mã tàu cụ thể để kiểm tra trạng thái không?";
            }

            // Kiểm tra xem đây là mã lịch hay mã tàu
            boolean isTrain = id.matches("[A-Za-z]+\\d+");

            if (isTrain) {
                // Tìm kiếm thông tin tàu theo mã
                List<LichTrinhTau> trainSchedules = findSchedulesByTrainId(id);

                if (trainSchedules.isEmpty()) {
                    return "Tôi không tìm thấy thông tin về tàu có mã " + id + ". Vui lòng kiểm tra lại mã tàu.";
                }

                // Nhóm theo trạng thái
                int daKhoiHanh = 0;
                int chuaKhoiHanh = 0;
                int daHuy = 0;

                for (LichTrinhTau lichTrinh : trainSchedules) {
                    if (lichTrinh.getTrangThai() == TrangThai.DA_KHOI_HANH) {
                        daKhoiHanh++;
                    } else if (lichTrinh.getTrangThai() == TrangThai.CHUA_KHOI_HANH) {
                        chuaKhoiHanh++;
                    } else if (lichTrinh.getTrangThai() == TrangThai.DA_HUY) {
                        daHuy++;
                    }
                }

                StringBuilder response = new StringBuilder();
                response.append("Thông tin trạng thái của tàu ").append(id).append(":\n\n");
                response.append("- Tổng số lịch trình: ").append(trainSchedules.size()).append("\n");
                response.append("- Đã khởi hành: ").append(daKhoiHanh).append("\n");
                response.append("- Chưa khởi hành: ").append(chuaKhoiHanh).append("\n");
                response.append("- Đã hủy: ").append(daHuy).append("\n\n");

                // Hiển thị lịch trình gần nhất
                if (chuaKhoiHanh > 0) {
                    LichTrinhTau nextSchedule = null;
                    for (LichTrinhTau lichTrinh : trainSchedules) {
                        if (lichTrinh.getTrangThai() == TrangThai.CHUA_KHOI_HANH) {
                            if (nextSchedule == null || lichTrinh.getNgayDi().isBefore(nextSchedule.getNgayDi()) ||
                                    (lichTrinh.getNgayDi().equals(nextSchedule.getNgayDi()) && lichTrinh.getGioDi().isBefore(nextSchedule.getGioDi()))) {
                                nextSchedule = lichTrinh;
                            }
                        }
                    }

                    if (nextSchedule != null) {
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                        response.append("Lịch trình sắp tới:\n");
                        response.append("Mã lịch: ").append(nextSchedule.getMaLich()).append("\n");
                        response.append("Ngày đi: ").append(nextSchedule.getNgayDi().format(dateFormatter)).append("\n");
                        response.append("Giờ đi: ").append(nextSchedule.getGioDi().format(timeFormatter)).append("\n");

                        // Thêm thời gian đến dự kiến bằng AI
                        try {
                            AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(nextSchedule);
                            response.append("Giờ đến dự kiến: ")
                                    .append(prediction.getEstimatedArrivalTime(nextSchedule.getGioDi()).format(timeFormatter));
                        } catch (Exception e) {
                            response.append("Giờ đến: Đang cập nhật");
                        }
                    }
                }

                return response.toString();

            } else {
                // Tìm kiếm thông tin lịch trình theo mã
                LichTrinhTau lichTrinh = lichTrinhTauDAO.getById(id);

                if (lichTrinh == null) {
                    return "Tôi không tìm thấy lịch trình có mã " + id + ". Vui lòng kiểm tra lại mã lịch trình.";
                }

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                StringBuilder response = new StringBuilder();
                response.append("Thông tin lịch trình ").append(id).append(":\n\n");
                response.append("- Tàu: ").append(lichTrinh.getTau().getMaTau()).append(" - ").append(lichTrinh.getTau().getTenTau()).append("\n");
                response.append("- Tuyến: ").append(lichTrinh.getTau().getTuyenTau().getGaDi())
                        .append(" → ").append(lichTrinh.getTau().getTuyenTau().getGaDen()).append("\n");
                response.append("- Ngày đi: ").append(lichTrinh.getNgayDi().format(dateFormatter)).append("\n");
                response.append("- Giờ đi: ").append(lichTrinh.getGioDi().format(timeFormatter)).append("\n");

                // Thêm thời gian đến dự kiến bằng AI
                try {
                    AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(lichTrinh);
                    response.append("- Giờ đến dự kiến: ")
                            .append(prediction.getEstimatedArrivalTime(lichTrinh.getGioDi()).format(timeFormatter)).append("\n");
                } catch (Exception e) {
                    response.append("- Giờ đến: Đang cập nhật\n");
                }

                response.append("- Trạng thái: ").append(lichTrinh.getTrangThai().getValue()).append("\n");

                return response.toString();
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn trạng thái: " + e.getMessage(), e);
            return "Xin lỗi, tôi không thể kiểm tra trạng thái vào lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Tìm kiếm lịch trình theo mã tàu
     */
    private List<LichTrinhTau> findSchedulesByTrainId(String trainId) {
        try {
            List<LichTrinhTau> allSchedules = lichTrinhTauDAO.getAllList();
            List<LichTrinhTau> filteredSchedules = new ArrayList<>();

            for (LichTrinhTau lichTrinh : allSchedules) {
                if (lichTrinh.getTau().getMaTau().equalsIgnoreCase(trainId)) {
                    filteredSchedules.add(lichTrinh);
                }
            }

            return filteredSchedules;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi khi tìm kiếm lịch trình theo mã tàu: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Trích xuất ID (mã lịch trình hoặc mã tàu) từ tin nhắn
     */
    private String extractId(String message) {
        message = message.toLowerCase();

        // Tìm kiếm mẫu: chữ + số (mã tàu)
        Pattern trainPattern = Pattern.compile("([a-z]{2,3}\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher trainMatcher = trainPattern.matcher(message);
        if (trainMatcher.find()) {
            return trainMatcher.group(1).toUpperCase();
        }

        // Tìm kiếm mẫu: mã lịch (thường là số)
        Pattern schedulePattern = Pattern.compile("(lịch|mã lịch|mã|id)\\s*(là)?\\s*([a-z0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher scheduleMatcher = schedulePattern.matcher(message);
        if (scheduleMatcher.find()) {
            return scheduleMatcher.group(3);
        }

        // Tìm kiếm số trong tin nhắn (có thể là mã lịch)
        Pattern numberPattern = Pattern.compile("\\b([a-z0-9]+)\\b");
        Matcher numberMatcher = numberPattern.matcher(message);
        while (numberMatcher.find()) {
            String potentialId = numberMatcher.group(1);
            if (potentialId.matches(".*\\d.*")) {  // Đảm bảo có ít nhất 1 chữ số
                return potentialId;
            }
        }

        return null;
    }

    /**
     * Xử lý truy vấn về ga tàu
     */
    private String handleStationQuery(String message) {
        // Thông tin ga tàu cơ bản
        Map<String, String[]> stationInfo = new java.util.HashMap<>();
        stationInfo.put("hà nội", new String[] {
                "Ga Hà Nội",
                "120 Lê Duẩn, Khâm Thiên, Đống Đa, Hà Nội",
                "024.3825.3949"
        });
        stationInfo.put("sài gòn", new String[] {
                "Ga Sài Gòn",
                "1 Nguyễn Thông, Phường 9, Quận 3, TP. Hồ Chí Minh",
                "028.3846.6119"
        });
        stationInfo.put("đà nẵng", new String[] {
                "Ga Đà Nẵng",
                "791 Hải Phòng, Tam Thuận, Thanh Khê, Đà Nẵng",
                "0236.3827.070"
        });
        // Có thể thêm nhiều ga khác...

        // Tìm ga tàu trong tin nhắn
        String station = null;
        for (String key : stationInfo.keySet()) {
            if (message.toLowerCase().contains(key)) {
                station = key;
                break;
            }
        }

        if (station != null) {
            String[] info = stationInfo.get(station);
            return "Thông tin về " + info[0] + ":\n\n" +
                    "- Địa chỉ: " + info[1] + "\n" +
                    "- Điện thoại: " + info[2] + "\n\n" +
                    "Bạn cần tìm lịch trình tàu từ ga này không?";
        } else {
            return "Bạn muốn biết thông tin về ga tàu nào? Tôi có thông tin về các ga chính như Hà Nội, Sài Gòn, Đà Nẵng.";
        }
    }

    /**
     * Xử lý truy vấn về tàu
     */
    private String handleTrainQuery(String message) {
        try {
            // Tìm ID tàu trong tin nhắn
            String trainId = extractId(message);

            if (trainId == null) {
                return getGenericTrainInfo();
            }

            // Tìm kiếm thông tin tàu theo ID
            Tau train = findTrainById(trainId);

            if (train == null) {
                return "Tôi không tìm thấy thông tin về tàu có mã " + trainId + ". " +
                        "Vui lòng kiểm tra lại mã tàu hoặc hỏi về tàu khác.";
            }

            StringBuilder response = new StringBuilder();
            response.append("Thông tin về tàu ").append(train.getMaTau()).append(":\n\n");
            response.append("- Tên tàu: ").append(train.getTenTau()).append("\n");
            response.append("- Tuyến: ").append(train.getTuyenTau().getGaDi())
                    .append(" → ").append(train.getTuyenTau().getGaDen()).append("\n");

            // Tìm các lịch trình sắp tới của tàu này
            List<LichTrinhTau> upcomingSchedules = findUpcomingSchedulesForTrain(train.getMaTau());

            if (!upcomingSchedules.isEmpty()) {
                response.append("\nCác lịch trình sắp tới:\n");

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                int count = Math.min(upcomingSchedules.size(), 3);
                for (int i = 0; i < count; i++) {
                    LichTrinhTau lichTrinh = upcomingSchedules.get(i);
                    response.append(i + 1).append(". Ngày ").append(lichTrinh.getNgayDi().format(dateFormatter))
                            .append(" - Giờ đi: ").append(lichTrinh.getGioDi().format(timeFormatter))
                            .append(" - Mã lịch: ").append(lichTrinh.getMaLich())
                            .append("\n");
                }
            }

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn tàu: " + e.getMessage(), e);
            return "Xin lỗi, tôi không thể tìm thông tin tàu vào lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Tìm thông tin tàu theo ID
     */
    private Tau findTrainById(String trainId) {
        try {
            List<LichTrinhTau> allSchedules = lichTrinhTauDAO.getAllList();

            for (LichTrinhTau lichTrinh : allSchedules) {
                if (lichTrinh.getTau().getMaTau().equalsIgnoreCase(trainId)) {
                    return lichTrinh.getTau();
                }
            }

            return null;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi khi tìm kiếm tàu: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Tìm các lịch trình sắp tới của một tàu
     */
    private List<LichTrinhTau> findUpcomingSchedulesForTrain(String trainId) {
        try {
            List<LichTrinhTau> allSchedules = lichTrinhTauDAO.getAllList();
            List<LichTrinhTau> upcomingSchedules = new ArrayList<>();

            LocalDate today = LocalDate.now();

            for (LichTrinhTau lichTrinh : allSchedules) {
                if (lichTrinh.getTau().getMaTau().equalsIgnoreCase(trainId) &&
                        lichTrinh.getTrangThai() == TrangThai.CHUA_KHOI_HANH &&
                        (lichTrinh.getNgayDi().isEqual(today) || lichTrinh.getNgayDi().isAfter(today))) {
                    upcomingSchedules.add(lichTrinh);
                }
            }

            // Sắp xếp theo ngày và giờ đi
            upcomingSchedules.sort((a, b) -> {
                int dateCompare = a.getNgayDi().compareTo(b.getNgayDi());
                if (dateCompare != 0) return dateCompare;
                return a.getGioDi().compareTo(b.getGioDi());
            });

            return upcomingSchedules;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi khi tìm kiếm lịch trình sắp tới: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Trả về thông tin chung về tàu
     */
    private String getGenericTrainInfo() {
        return "Các mã tàu thường bắt đầu bằng chữ cái cho biết loại tàu:\n\n" +
                "- SE: Tàu tốc hành (Reunification Express)\n" +
                "- TN: Tàu nhanh\n" +
                "- LP: Tàu địa phương\n\n" +
                "Để tìm thông tin về một tàu cụ thể, vui lòng cung cấp mã tàu.";
    }

    /**
     * Xử lý truy vấn không xác định
     */
    private String handleUnknownQuery() {
        String[] responses = {
                "Xin lỗi, tôi không hiểu rõ câu hỏi của bạn. Bạn có thể hỏi về lịch trình tàu, trạng thái tàu, hoặc thông tin ga tàu.",
                "Tôi không chắc tôi hiểu ý bạn. Bạn có thể thử hỏi như: 'Lịch trình tàu từ Hà Nội đến Đà Nẵng', 'Trạng thái tàu SE2', hoặc 'Thông tin ga Sài Gòn'.",
                "Câu hỏi của bạn hơi khó hiểu đối với tôi. Tôi có thể giúp bạn tìm lịch trình tàu, kiểm tra trạng thái tàu hoặc cung cấp thông tin về ga tàu.",
                "Tôi chưa được đào tạo để trả lời câu hỏi này. Bạn có thể hỏi tôi về lịch trình tàu, thông tin ga hoặc trạng thái chuyến tàu không?"
        };

        return getRandomResponse(responses);
    }

    /**
     * Lấy một phản hồi ngẫu nhiên từ danh sách các phản hồi có sẵn
     */
    private String getRandomResponse(String[] responses) {
        int index = (int) (Math.random() * responses.length);
        return responses[index];
    }
}