package service;

import dao.LichTrinhTauDAO;
import model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
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
    // Thêm các trường mới để lưu trữ dữ liệu huấn luyện
    private Map<String, List<TrainingQuery>> intentMap = new HashMap<>();
    private Map<String, List<String>> responseTemplates = new HashMap<>();
    // Từ khóa để xác định loại tin nhắn
    // Thêm nhiều từ khóa và biến thể vào các mảng hiện có
    private final String[] SCHEDULE_KEYWORDS = {
            "lịch trình", "lịch tàu", "chuyến tàu", "khi nào", "mấy giờ", "ngày nào",
            "tàu chạy lúc mấy giờ", "giờ tàu", "lịch chạy", "thời gian khởi hành",
            "tàu nào sắp chạy", "có tàu nào", "khi nào có tàu"
    };
    private final String[] STATUS_KEYWORDS = {"trạng thái", "tình trạng", "đã chạy", "đã khởi hành", "còn chạy"};
    private final String[] STATION_KEYWORDS = {"ga", "nhà ga", "điểm đến", "điểm khởi hành"};
    private final String[] TRAIN_KEYWORDS = {"tàu", "loại tàu", "tàu gì", "tàu nào"};
    private final String[] GREETING_KEYWORDS = {"xin chào", "chào", "hello", "hi", "hey"};
    private final String[] THANKS_KEYWORDS = {"cảm ơn", "thanks", "thank", "cám ơn"};
    private final String[] GOODBYE_KEYWORDS = {"tạm biệt", "bye", "goodbye", "gặp lại sau"};

    public ChatbotEngine(LichTrinhTauDAO lichTrinhTauDAO) {
        this.lichTrinhTauDAO = lichTrinhTauDAO;
        this.aiPredictor = AITravelTimePredictor.getInstance();
        loadTrainingData();
    }

    /**
     * Xử lý tin nhắn từ người dùng và trả về câu trả lời
     * @param message Tin nhắn từ người dùng
     * @return Câu trả lời
     */
    public String processMessage(String message) {
        try {
            // Bước 1: Phân loại ý định của tin nhắn
            String intent = classifyIntent(message);

            // Bước 2: Trích xuất thực thể từ tin nhắn
            Map<String, String> entities = extractEntitiesWithTrainingData(message, intent);

            // Bước 3: Xử lý và trả về câu trả lời dựa trên intent
            switch (intent) {
                case "greeting":
                    return handleGreeting(message);
                case "thanks":
                    return handleThanks();
                case "goodbye":
                    return handleGoodbye();
                case "schedule_query":
                    return handleScheduleQuery(message, entities);
                case "status_query":
                    return handleStatusQuery(message, entities);
                case "station_query":
                    return handleStationQuery(message, entities);
                case "train_query":
                    return handleTrainQuery(message, entities);
                case "ticket_query":
                    return handleTicketQuery(message, entities);
                default:
                    // Sử dụng mẫu câu trả lời cho intent không xác định
                    return selectResponseTemplate("fallback", entities);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý tin nhắn: " + e.getMessage(), e);
            return "Xin lỗi, đã xảy ra lỗi khi xử lý tin nhắn của bạn. Vui lòng thử lại với câu hỏi khác.";
        }
    }

    /**
     * Xử lý truy vấn về vé tàu
     */
    private String handleTicketQuery(String message, Map<String, String> entities) {
        // Các câu hỏi về giá vé
        if (message.toLowerCase().contains("giá") || message.toLowerCase().contains("bao nhiêu")) {
            String gaDi = entities.getOrDefault("ga_di", null);
            String gaDen = entities.getOrDefault("ga_den", null);

            if (gaDi != null && gaDen != null) {
                // Ở đây bạn có thể thêm logic tìm kiếm giá vé theo ga đi và ga đến
                return "Giá vé tàu từ " + gaDi + " đến " + gaDen + " dao động từ 300.000đ đến 1.200.000đ tùy loại tàu và loại ghế.";
            }
        }

        // Câu hỏi về đặt vé
        if (message.toLowerCase().contains("đặt vé") || message.toLowerCase().contains("mua vé")) {
            return "Bạn có thể đặt vé tàu thông qua website, ứng dụng di động hoặc trực tiếp tại các ga tàu. " +
                    "Để đặt vé, bạn cần cung cấp thông tin cá nhân, chọn lịch trình và chỗ ngồi mong muốn.";
        }

        // Câu hỏi về đổi/hủy vé
        if (message.toLowerCase().contains("đổi vé") || message.toLowerCase().contains("hủy vé") ||
                message.toLowerCase().contains("hoàn tiền") || message.toLowerCase().contains("hoàn vé")) {
            return "Để đổi hoặc hủy vé, bạn cần liên hệ với nơi đặt vé trước ít nhất 24 giờ so với giờ tàu chạy. " +
                    "Phí đổi/hủy vé sẽ áp dụng theo quy định hiện hành.";
        }

        // Câu hỏi về khuyến mãi
        if (message.toLowerCase().contains("khuyến mãi") || message.toLowerCase().contains("giảm giá")) {
            return "Hiện tại chúng tôi có các chương trình khuyến mãi như giảm 10% cho sinh viên, giảm 15% cho người cao tuổi, " +
                    "và các ưu đãi theo mùa. Bạn có thể kiểm tra chi tiết tại website hoặc liên hệ trực tiếp với nhân viên.";
        }

        // Trả lời chung
        return "Để biết thông tin đầy đủ về vé tàu, xin vui lòng truy cập website chính thức hoặc liên hệ tổng đài 19001730.";
    }

    /**
     * Xử lý truy vấn về lịch trình
     */
    private String handleScheduleQuery(String message, Map<String, String> entities) {
        try {
            // Lấy thông tin từ entities đã trích xuất
            String gaDi = entities.getOrDefault("ga_di", null);
            String gaDen = entities.getOrDefault("ga_den", null);
            LocalDate ngayDi = null;

            // Chuyển đổi ngày từ chuỗi sang LocalDate nếu có
            if (entities.containsKey("ngay_di")) {
                try {
                    ngayDi = LocalDate.parse(entities.get("ngay_di"));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Không thể chuyển đổi ngày: " + e.getMessage(), e);
                }
            }

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
     * Xử lý truy vấn về trạng thái
     */
    private String handleStatusQuery(String message, Map<String, String> entities) {
        try {
            // Tìm ID tàu hoặc lịch trình
            String id;
            if (entities.containsKey("ma_tau")) {
                id = entities.get("ma_tau");
            } else if (entities.containsKey("ma_lich")) {
                id = entities.get("ma_lich");
            } else {
                id = extractId(message);
            }

            if (id == null) {
                return "Bạn có thể cho tôi biết mã lịch trình hoặc mã tàu cụ thể để kiểm tra trạng thái không?";
            }

            // Kiểm tra xem đây là mã lịch hay mã tàu
            boolean isTrain = id.matches("[A-Za-z]+\\d+");

            if (isTrain) {
                return handleTrainStatusQuery(id);
            } else {
                return handleScheduleStatusQuery(id);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn trạng thái: " + e.getMessage(), e);
            return "Xin lỗi, tôi không thể kiểm tra trạng thái vào lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Xử lý truy vấn trạng thái tàu
     */
    private String handleTrainStatusQuery(String trainId) {
        try {
            // Tìm kiếm thông tin tàu theo mã
            List<LichTrinhTau> trainSchedules = findSchedulesByTrainId(trainId);

            if (trainSchedules.isEmpty()) {
                return "Tôi không tìm thấy thông tin về tàu có mã " + trainId + ". Vui lòng kiểm tra lại mã tàu.";
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
            response.append("Thông tin trạng thái của tàu ").append(trainId).append(":\n\n");
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
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn trạng thái tàu: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Xử lý truy vấn trạng thái lịch trình
     */
    private String handleScheduleStatusQuery(String scheduleId) throws RemoteException {
        try {
            // Tìm kiếm thông tin lịch trình theo mã
            LichTrinhTau lichTrinh = lichTrinhTauDAO.getById(scheduleId);

            if (lichTrinh == null) {
                return "Tôi không tìm thấy lịch trình có mã " + scheduleId + ". Vui lòng kiểm tra lại mã lịch trình.";
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            StringBuilder response = new StringBuilder();
            response.append("Thông tin lịch trình ").append(scheduleId).append(":\n\n");
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
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn trạng thái lịch trình: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Xử lý truy vấn về ga tàu
     */
    private String handleStationQuery(String message, Map<String, String> entities) {
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

        // Tìm ga tàu trong entities hoặc tin nhắn
        String station = entities.getOrDefault("ga", null);

        if (station == null) {
            // Tìm ga tàu trong tin nhắn
            for (String key : stationInfo.keySet()) {
                if (message.toLowerCase().contains(key)) {
                    station = key;
                    break;
                }
            }
        }

        if (station != null) {
            String[] info = stationInfo.get(station.toLowerCase());
            if (info != null) {
                return "Thông tin về " + info[0] + ":\n\n" +
                        "- Địa chỉ: " + info[1] + "\n" +
                        "- Điện thoại: " + info[2] + "\n\n" +
                        "Bạn cần tìm lịch trình tàu từ ga này không?";
            }
        }

        return "Bạn muốn biết thông tin về ga tàu nào? Tôi có thông tin về các ga chính như Hà Nội, Sài Gòn, Đà Nẵng.";
    }

    /**
     * Xử lý truy vấn về tàu
     */
    private String handleTrainQuery(String message, Map<String, String> entities) {
        try {
            // Tìm ID tàu trong entities hoặc tin nhắn
            String trainId = entities.getOrDefault("ma_tau", null);

            if (trainId == null) {
                trainId = extractId(message);
            }

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
     * Cập nhật dữ liệu huấn luyện từ phản hồi người dùng
     * @param userMessage Tin nhắn người dùng
     * @param botResponse Câu trả lời của bot
     * @param isHelpful Phản hồi có hữu ích không
     * @param intent Intent được phân loại
     */
    public void learnFromFeedback(String userMessage, String botResponse, boolean isHelpful, String intent) {
        if (!isHelpful || intent.equals("unknown")) {
            // Nếu phản hồi không hữu ích, lưu lại để phân tích và cải thiện sau này
            try {
                File feedbackDir = new File("js");
                if (!feedbackDir.exists()) {
                    feedbackDir.mkdirs();
                }

                File feedbackFile = new File("js/feedback_data.txt");
                try (FileWriter fw = new FileWriter(feedbackFile, true);
                     BufferedWriter bw = new BufferedWriter(fw);
                     PrintWriter out = new PrintWriter(bw)) {
                    out.println("USER: " + userMessage);
                    out.println("BOT: " + botResponse);
                    out.println("HELPFUL: " + isHelpful);
                    out.println("INTENT: " + intent);
                    out.println("TIMESTAMP: " + LocalDateTime.now());
                    out.println("---");
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Không thể lưu phản hồi: " + e.getMessage(), e);
            }
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

    /**
     * Tải dữ liệu huấn luyện từ file JSON
     */
    public void loadTrainingData() {
        try {
            File trainingFile = new File("training_data.json");
            if (!trainingFile.exists()) {
                LOGGER.warning("File training_data.json không tồn tại. Tạo file mặc định...");
                createDefaultTrainingData();
                return;
            }

            // Đọc file JSON chứa các cặp câu hỏi-câu trả lời
            String jsonContent = new String(Files.readAllBytes(Paths.get("training_data.json")));
            JSONObject trainingData = new JSONObject(jsonContent);

            // Xử lý dữ liệu intents
            JSONObject intentsObj = trainingData.getJSONObject("intents");
            for (String intentName : intentsObj.keySet()) {
                List<TrainingQuery> queries = new ArrayList<>();
                JSONArray queriesArray = intentsObj.getJSONArray(intentName);

                for (int i = 0; i < queriesArray.length(); i++) {
                    JSONObject queryObj = queriesArray.getJSONObject(i);
                    TrainingQuery trainingQuery = new TrainingQuery();
                    trainingQuery.setQuery(queryObj.getString("query"));

                    // Xử lý entities nếu có
                    if (queryObj.has("entities")) {
                        JSONObject entitiesObj = queryObj.getJSONObject("entities");
                        Map<String, String> entities = new HashMap<>();

                        for (String entityName : entitiesObj.keySet()) {
                            entities.put(entityName, entitiesObj.getString(entityName));
                        }

                        trainingQuery.setEntities(entities);
                    } else {
                        trainingQuery.setEntities(new HashMap<>());
                    }

                    queries.add(trainingQuery);
                }

                intentMap.put(intentName, queries);
            }

            // Xử lý dữ liệu responses
            JSONObject responsesObj = trainingData.getJSONObject("responses");
            for (String responseType : responsesObj.keySet()) {
                List<String> templates = new ArrayList<>();
                JSONArray templatesArray = responsesObj.getJSONArray(responseType);

                for (int i = 0; i < templatesArray.length(); i++) {
                    templates.add(templatesArray.getString(i));
                }

                responseTemplates.put(responseType, templates);
            }

            LOGGER.info("Đã tải dữ liệu huấn luyện: " + intentMap.size() + " intents, " +
                    responseTemplates.size() + " response types");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải dữ liệu huấn luyện: " + e.getMessage(), e);
            createDefaultTrainingData();
        }
    }

    /**
     * Tạo file dữ liệu huấn luyện mặc định nếu không tồn tại
     */
    private void createDefaultTrainingData() {
        try {
            JSONObject trainingData = new JSONObject();

            // Tạo các intents
            JSONObject intents = new JSONObject();

            // Greeting intent
            JSONArray greetingQueries = new JSONArray();
            greetingQueries.put(new JSONObject().put("query", "Xin chào").put("entities", new JSONObject()));
            greetingQueries.put(new JSONObject().put("query", "Chào bạn").put("entities", new JSONObject()));
            greetingQueries.put(new JSONObject().put("query", "Hello").put("entities", new JSONObject()));
            intents.put("greeting", greetingQueries);

            // Thanks intent
            JSONArray thanksQueries = new JSONArray();
            thanksQueries.put(new JSONObject().put("query", "Cảm ơn").put("entities", new JSONObject()));
            thanksQueries.put(new JSONObject().put("query", "Cám ơn bạn nhiều").put("entities", new JSONObject()));
            thanksQueries.put(new JSONObject().put("query", "Thank you").put("entities", new JSONObject()));
            intents.put("thanks", thanksQueries);

            // Goodbye intent
            JSONArray goodbyeQueries = new JSONArray();
            goodbyeQueries.put(new JSONObject().put("query", "Tạm biệt").put("entities", new JSONObject()));
            goodbyeQueries.put(new JSONObject().put("query", "Bye").put("entities", new JSONObject()));
            goodbyeQueries.put(new JSONObject().put("query", "Hẹn gặp lại").put("entities", new JSONObject()));
            intents.put("goodbye", goodbyeQueries);

            // Schedule query intent
            JSONArray scheduleQueries = new JSONArray();
            JSONObject query1 = new JSONObject();
            query1.put("query", "Cho tôi lịch tàu từ Hà Nội đến Đà Nẵng");
            JSONObject entities1 = new JSONObject();
            entities1.put("ga_di", "Hà Nội");
            entities1.put("ga_den", "Đà Nẵng");
            query1.put("entities", entities1);
            scheduleQueries.put(query1);

            JSONObject query2 = new JSONObject();
            query2.put("query", "Có tàu nào đi Sài Gòn ngày mai không");
            JSONObject entities2 = new JSONObject();
            entities2.put("ga_den", "Sài Gòn");
            entities2.put("ngay_di", "ngày mai");
            query2.put("entities", entities2);
            scheduleQueries.put(query2);

            intents.put("schedule_query", scheduleQueries);

            // Thêm các intents khác
            // Status query
            JSONArray statusQueries = new JSONArray();
            statusQueries.put(new JSONObject()
                    .put("query", "Trạng thái của tàu SE2")
                    .put("entities", new JSONObject().put("ma_tau", "SE2")));
            statusQueries.put(new JSONObject()
                    .put("query", "Tàu SE3 đã khởi hành chưa")
                    .put("entities", new JSONObject().put("ma_tau", "SE3")));
            intents.put("status_query", statusQueries);

            // Station query
            JSONArray stationQueries = new JSONArray();
            stationQueries.put(new JSONObject()
                    .put("query", "Thông tin về ga Hà Nội")
                    .put("entities", new JSONObject().put("ga", "Hà Nội")));
            stationQueries.put(new JSONObject()
                    .put("query", "Cho tôi địa chỉ ga Sài Gòn")
                    .put("entities", new JSONObject().put("ga", "Sài Gòn")));
            intents.put("station_query", stationQueries);

            // Train query
            JSONArray trainQueries = new JSONArray();
            trainQueries.put(new JSONObject()
                    .put("query", "Thông tin về tàu SE2")
                    .put("entities", new JSONObject().put("ma_tau", "SE2")));
            trainQueries.put(new JSONObject()
                    .put("query", "Tàu TN4 có chạy tuyến nào")
                    .put("entities", new JSONObject().put("ma_tau", "TN4")));
            intents.put("train_query", trainQueries);

            trainingData.put("intents", intents);

            // Tạo responses
            JSONObject responses = new JSONObject();

            // Greeting responses
            JSONArray greetingResponses = new JSONArray();
            greetingResponses.put("Xin chào! Tôi có thể giúp gì cho bạn về lịch trình tàu hỏa?");
            greetingResponses.put("Chào bạn! Bạn cần tìm thông tin gì về lịch trình tàu?");
            responses.put("greeting", greetingResponses);

            // Thanks responses
            JSONArray thanksResponses = new JSONArray();
            thanksResponses.put("Không có gì! Rất vui khi được giúp bạn.");
            thanksResponses.put("Không có chi! Bạn cần hỗ trợ gì thêm không?");
            responses.put("thanks", thanksResponses);

            // Goodbye responses
            JSONArray goodbyeResponses = new JSONArray();
            goodbyeResponses.put("Tạm biệt! Rất vui được hỗ trợ bạn.");
            goodbyeResponses.put("Chúc bạn một ngày tốt lành! Hẹn gặp lại.");
            responses.put("goodbye", goodbyeResponses);

            // Fallback responses
            JSONArray fallbackResponses = new JSONArray();
            fallbackResponses.put("Xin lỗi, tôi không hiểu ý bạn. Bạn có thể nói rõ hơn được không?");
            fallbackResponses.put("Tôi chưa được đào tạo để hiểu câu hỏi này. Bạn có thể hỏi về lịch trình tàu, trạng thái tàu, hoặc thông tin ga.");
            responses.put("fallback", fallbackResponses);

            trainingData.put("responses", responses);

            // Lưu file
            try (FileWriter file = new FileWriter("training_data.json")) {
                file.write(trainingData.toString(4)); // indent = 4 spaces
                LOGGER.info("Đã tạo file training_data.json mặc định");
            }

            // Đọc file vừa tạo
            loadTrainingData();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Không thể tạo file training_data.json mặc định: " + e.getMessage(), e);
        }
    }

    /**
     * Phân loại ý định của tin nhắn dựa trên dữ liệu huấn luyện
     * @param message Tin nhắn cần phân loại
     * @return Tên của ý định được phân loại
     */
    private String classifyIntent(String message) {
        if (intentMap.isEmpty()) {
            // Nếu không có dữ liệu huấn luyện, sử dụng phương pháp cũ
            return classifyMessageByKeywords(message);
        }

        String lowercaseMessage = message.toLowerCase();
        String bestIntent = "unknown";
        double bestScore = 0.0;

        // Tính điểm tương đồng cho mỗi intent
        for (Map.Entry<String, List<TrainingQuery>> entry : intentMap.entrySet()) {
            String intent = entry.getKey();
            List<TrainingQuery> queries = entry.getValue();

            for (TrainingQuery query : queries) {
                double score = calculateSimilarity(lowercaseMessage, query.getQuery().toLowerCase());
                if (score > bestScore) {
                    bestScore = score;
                    bestIntent = intent;
                }
            }
        }

        // Nếu điểm tương đồng quá thấp, trả về unknown
        if (bestScore < 0.4) {
            return "unknown";
        }

        return bestIntent;
    }

    /**
     * Tính điểm tương đồng giữa hai chuỗi
     * @param s1 Chuỗi thứ nhất
     * @param s2 Chuỗi thứ hai
     * @return Điểm tương đồng (0.0 - 1.0)
     */
    private double calculateSimilarity(String s1, String s2) {
        // Tính toán tương đồng bằng Jaccard Similarity
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.split("\\s+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) return 0.0;

        return (double) intersection.size() / union.size();
    }

    /**
     * Phân loại tin nhắn dựa trên từ khóa (phương pháp cũ)
     */
    private String classifyMessageByKeywords(String message) {
        String lowercaseMessage = message.toLowerCase();

        if (containsAny(lowercaseMessage, GREETING_KEYWORDS)) return "greeting";
        if (containsAny(lowercaseMessage, THANKS_KEYWORDS)) return "thanks";
        if (containsAny(lowercaseMessage, GOODBYE_KEYWORDS)) return "goodbye";
        if (containsAny(lowercaseMessage, SCHEDULE_KEYWORDS)) return "schedule_query";
        if (containsAny(lowercaseMessage, STATUS_KEYWORDS)) return "status_query";
        if (containsAny(lowercaseMessage, STATION_KEYWORDS)) return "station_query";
        if (containsAny(lowercaseMessage, TRAIN_KEYWORDS)) return "train_query";

        return "unknown";
    }

    /**
     * Trích xuất thực thể từ tin nhắn dựa trên dữ liệu huấn luyện
     * @param message Tin nhắn cần trích xuất thực thể
     * @param intent Intent đã được phân loại
     * @return Map chứa các thực thể đã trích xuất
     */
    private Map<String, String> extractEntitiesWithTrainingData(String message, String intent) {
        Map<String, String> entities = new HashMap<>();
        List<TrainingQuery> queries = intentMap.getOrDefault(intent, new ArrayList<>());

        // Xử lý theo phương pháp cũ nếu không có dữ liệu huấn luyện
        if (queries.isEmpty()) {
            // Trích xuất thông tin địa điểm
            String gaDi = extractLocation(message, "từ", "đi từ", "khởi hành từ");
            String gaDen = extractLocation(message, "đến", "tới", "về");

            // Tìm kiếm thông tin ngày
            LocalDate ngayDi = extractDate(message);

            if (gaDi != null) entities.put("ga_di", gaDi);
            if (gaDen != null) entities.put("ga_den", gaDen);
            if (ngayDi != null) entities.put("ngay_di", ngayDi.toString());

            return entities;
        }

        // Sử dụng dữ liệu huấn luyện để trích xuất thực thể
        String lowercaseMessage = message.toLowerCase();
        TrainingQuery bestMatchQuery = null;
        double bestScore = 0.0;

        // Tìm training query tương đồng nhất
        for (TrainingQuery query : queries) {
            double score = calculateSimilarity(lowercaseMessage, query.getQuery().toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestMatchQuery = query;
            }
        }

        // Nếu có training query tương đồng và có entities
        if (bestMatchQuery != null && bestMatchQuery.getEntities() != null) {
            // Dùng thông tin thực thể từ training query để trích xuất từ tin nhắn
            for (Map.Entry<String, String> entry : bestMatchQuery.getEntities().entrySet()) {
                String entityName = entry.getKey();

                // Tìm giá trị thực thể trong tin nhắn người dùng
                if (entityName.equals("ga_di")) {
                    String gaDi = extractLocation(message, "từ", "đi từ", "khởi hành từ");
                    if (gaDi != null) entities.put("ga_di", gaDi);
                } else if (entityName.equals("ga_den")) {
                    String gaDen = extractLocation(message, "đến", "tới", "về");
                    if (gaDen != null) entities.put("ga_den", gaDen);
                } else if (entityName.equals("ngay_di")) {
                    LocalDate ngayDi = extractDate(message);
                    if (ngayDi != null) entities.put("ngay_di", ngayDi.toString());
                } else if (entityName.equals("ten_tau")) {
                    String tenTau = extractTrainName(message);
                    if (tenTau != null) entities.put("ten_tau", tenTau);
                } else if (entityName.equals("ga")) {
                    for (String station : new String[]{"hà nội", "sài gòn", "đà nẵng", "huế", "nha trang"}) {
                        if (message.toLowerCase().contains(station)) {
                            entities.put("ga", capitalizeWords(station));
                            break;
                        }
                    }
                }
            }
        }

        return entities;
    }
    /**
     * Trích xuất tên tàu từ tin nhắn
     */
    private String extractTrainName(String message) {
        message = message.toLowerCase();

        // Tìm kiếm mẫu tên tàu phổ biến (SE2, TN4, LP6, v.v.)
        Pattern trainNamePattern = Pattern.compile("(se|tn|lp)[0-9]+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = trainNamePattern.matcher(message);

        if (matcher.find()) {
            return matcher.group().toUpperCase();
        }

        return null;
    }
    /**
     * Chọn mẫu câu trả lời dựa trên intent và entities
     */
    private String selectResponseTemplate(String intent, Map<String, String> entities) {
        // Lấy danh sách mẫu câu trả lời cho intent
        List<String> templates = responseTemplates.getOrDefault(intent,
                responseTemplates.getOrDefault("fallback", Collections.singletonList(
                        "Xin lỗi, tôi không hiểu ý bạn.")));

        // Chọn ngẫu nhiên một mẫu câu
        Random rand = new Random();
        String template = templates.get(rand.nextInt(templates.size()));

        // Thay thế các placeholders trong template với giá trị thực tế từ entities
        for (Map.Entry<String, String> entry : entities.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (template.contains(placeholder)) {
                template = template.replace(placeholder, entry.getValue());
            }
        }

        return template;
    }

    /**
     * Lớp lưu trữ câu hỏi huấn luyện
     */
    public static class TrainingQuery {
        private String query;
        private Map<String, String> entities;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public Map<String, String> getEntities() { return entities; }
        public void setEntities(Map<String, String> entities) { this.entities = entities; }
    }
}