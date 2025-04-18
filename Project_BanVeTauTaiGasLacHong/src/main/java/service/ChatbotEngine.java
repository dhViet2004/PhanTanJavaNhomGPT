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

    // Danh sách các ga tàu phổ biến với các biến thể
    private final Map<String, String> STATION_MAP = new HashMap<String, String>() {{
        put("hà nội", "Hà Nội");
        put("ha noi", "Hà Nội");
        put("hanoi", "Hà Nội");
        put("hn", "Hà Nội");

        put("sài gòn", "Sài Gòn");
        put("sai gon", "Sài Gòn");
        put("saigon", "Sài Gòn");
        put("tphcm", "Sài Gòn");
        put("tp hcm", "Sài Gòn");
        put("hồ chí minh", "Sài Gòn");
        put("ho chi minh", "Sài Gòn");
        put("sg", "Sài Gòn");

        put("đà nẵng", "Đà Nẵng");
        put("da nang", "Đà Nẵng");
        put("danang", "Đà Nẵng");
        put("dn", "Đà Nẵng");

        put("huế", "Huế");
        put("hue", "Huế");

        put("nha trang", "Nha Trang");
        put("nhatrang", "Nha Trang");
        put("nt", "Nha Trang");

        put("vinh", "Vinh");

        put("hải phòng", "Hải Phòng");
        put("hai phong", "Hải Phòng");
        put("haiphong", "Hải Phòng");
        put("hp", "Hải Phòng");

        put("thanh hóa", "Thanh Hóa");
        put("thanh hoa", "Thanh Hóa");
        put("thanhhoa", "Thanh Hóa");

        put("lạng sơn", "Lạng Sơn");
        put("lang son", "Lạng Sơn");
        put("langson", "Lạng Sơn");
        put("ls", "Lạng Sơn");

        put("hạ long", "Hạ Long");
        put("ha long", "Hạ Long");
        put("halong", "Hạ Long");
        put("hl", "Hạ Long");

        put("quảng ngãi", "Quảng Ngãi");
        put("quang ngai", "Quảng Ngãi");
        put("quangngai", "Quảng Ngãi");
        put("qn", "Quảng Ngãi");

        put("đồng hới", "Đồng Hới");
        put("dong hoi", "Đồng Hới");
        put("donghoi", "Đồng Hới");
        put("dh", "Đồng Hới");
    }};

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
     * Phân loại ý định của tin nhắn dựa trên dữ liệu huấn luyện và từ khóa
     * @param message Tin nhắn cần phân loại
     * @return Tên của ý định được phân loại
     */
    private String classifyIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "fallback";
        }

        // Chuẩn hóa tin nhắn
        String normalizedMessage = normalizeText(message);

        // Mảng lưu trữ điểm số cho mỗi intent
        Map<String, Double> intentScores = new HashMap<>();

        // Tính điểm cho mỗi intent từ dữ liệu training
        for (Map.Entry<String, List<TrainingQuery>> entry : intentMap.entrySet()) {
            String intent = entry.getKey();
            List<TrainingQuery> queries = entry.getValue();

            double maxScore = 0.0;
            for (TrainingQuery queryData : queries) {
                String query = queryData.getQuery();
                String normalizedQuery = normalizeText(query);

                // Tính điểm tương đồng giữa câu hỏi và mẫu
                double jaccardScore = calculateJaccardSimilarity(normalizedMessage, normalizedQuery);
                double ngramScore = calculateNGramSimilarity(normalizedMessage, normalizedQuery, 3);
                double keywordScore = calculateKeywordSimilarity(normalizedMessage, normalizedQuery);

                // Tính điểm tổng hợp với trọng số
                double score = jaccardScore * 0.3 + ngramScore * 0.3 + keywordScore * 0.4;

                // Cộng thêm điểm cho các từ khóa đặc biệt
                score += checkSpecialKeywords(normalizedMessage, intent);

                // Lưu điểm cao nhất cho intent này
                if (score > maxScore) {
                    maxScore = score;
                }
            }

            intentScores.put(intent, maxScore);
        }

        // Tìm intent có điểm cao nhất
        String bestIntent = "fallback";
        double highestScore = 0.25; // Ngưỡng tối thiểu để xác định intent

        for (Map.Entry<String, Double> score : intentScores.entrySet()) {
            LOGGER.fine("Intent: " + score.getKey() + ", Score: " + score.getValue());
            if (score.getValue() > highestScore) {
                highestScore = score.getValue();
                bestIntent = score.getKey();
            }
        }

        LOGGER.info("Phân loại tin nhắn: '" + message + "' -> " + bestIntent + " (score: " + highestScore + ")");
        return bestIntent;
    }

    /**
     * Kiểm tra các từ khóa đặc biệt trong tin nhắn để tính điểm thêm cho intent
     */
    private double checkSpecialKeywords(String message, String intent) {
        double score = 0.0;

        switch (intent) {
            case "schedule_query":
                // Từ khóa liên quan đến lịch trình
                if (containsAny(message, new String[]{
                        "lịch", "giờ", "chuyến", "tàu nào", "tàu từ", "đến", "tới", "đi",
                        "khi nào", "mấy giờ", "lúc mấy", "thời gian", "khởi hành", "xuất phát"})) {
                    score += 0.2;
                }
                // Kiểm tra nếu có tên ga
                if (containsAnyStation(message)) {
                    score += 0.3;
                }
                break;

            case "status_query":
                // Từ khóa liên quan đến trạng thái
                if (containsAny(message, new String[]{
                        "trạng thái", "tình trạng", "đã đi", "đã tới", "khởi hành", "đã đến",
                        "hủy", "hoãn", "trễ", "chưa", "có còn", "có chạy"})) {
                    score += 0.3;
                }
                // Kiểm tra nếu có mã tàu/tên tàu
                if (containsAnyTrain(message)) {
                    score += 0.3;
                }
                break;

            case "station_query":
                // Từ khóa liên quan đến ga
                if (containsAny(message, new String[]{
                        "ga", "nhà ga", "bến", "địa chỉ", "số điện thoại", "ở đâu",
                        "tiện ích", "nơi đón", "địa điểm"})) {
                    score += 0.3;
                }
                // Kiểm tra nếu có tên ga
                if (containsAnyStation(message)) {
                    score += 0.3;
                }
                break;

            case "train_query":
                // Từ khóa liên quan đến tàu
                if (containsAny(message, new String[]{
                        "tàu", "chạy", "tuyến", "toa", "ghế", "giường", "thông tin",
                        "loại tàu", "tàu gì", "tàu nào", "bao nhiêu", "toa tàu"})) {
                    score += 0.2;
                }
                // Kiểm tra nếu có mã tàu/tên tàu
                if (containsAnyTrain(message)) {
                    score += 0.4;
                }
                break;

            case "ticket_query":
                // Từ khóa liên quan đến vé
                if (containsAny(message, new String[]{
                        "vé", "giá", "đặt", "mua", "đổi", "hủy", "hoàn", "khuyến mãi",
                        "bao nhiêu tiền", "chi phí", "phí", "đồng", "nghìn", "triệu"})) {
                    score += 0.3;
                }
                break;

            case "greeting":
                if (containsAny(message, new String[]{
                        "xin chào", "chào", "hello", "hi", "hey", "alo", "này"})) {
                    score += 0.8;
                }
                break;

            case "thanks":
                if (containsAny(message, new String[]{
                        "cảm ơn", "cám ơn", "thank", "thanks", "tốt quá", "hữu ích"})) {
                    score += 0.8;
                }
                break;

            case "goodbye":
                if (containsAny(message, new String[]{
                        "tạm biệt", "bye", "gặp lại", "chào tạm biệt", "dừng",
                        "kết thúc", "đi đây", "thôi"})) {
                    score += 0.8;
                }
                break;
        }

        return score;
    }

    /**
     * Tính độ tương đồng từ khóa giữa hai chuỗi
     */
    private double calculateKeywordSimilarity(String message, String query) {
        // Tách thành các từ
        String[] messageWords = message.split("\\s+");
        String[] queryWords = query.split("\\s+");

        // Đếm số từ khớp
        int matches = 0;
        for (String queryWord : queryWords) {
            for (String messageWord : messageWords) {
                // Kiểm tra từ giống nhau hoặc là tiền tố của nhau
                if (messageWord.equals(queryWord) ||
                        (messageWord.length() > 3 && queryWord.length() > 3 &&
                                (messageWord.startsWith(queryWord) || queryWord.startsWith(messageWord)))) {
                    matches++;
                    break;
                }
            }
        }

        // Tính điểm dựa trên tỷ lệ từ khớp
        double wordMatchRatio = (double) matches / queryWords.length;

        return wordMatchRatio;
    }

    /**
     * Tính độ tương đồng Jaccard giữa hai chuỗi
     */
    private double calculateJaccardSimilarity(String str1, String str2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(str1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(str2.split("\\s+")));

        // Tính số phần tử giao nhau
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // Tính số phần tử hợp
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        // Trả về tỷ lệ
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    /**
     * Tính độ tương đồng dựa trên n-gram
     */
    private double calculateNGramSimilarity(String str1, String str2, int n) {
        List<String> ngrams1 = generateNGrams(str1, n);
        List<String> ngrams2 = generateNGrams(str2, n);

        // Đếm số n-gram chung
        int commonCount = 0;
        for (String ngram : ngrams1) {
            if (ngrams2.contains(ngram)) {
                commonCount++;
            }
        }

        // Tính độ tương đồng
        int totalNGrams = ngrams1.size() + ngrams2.size();
        if (totalNGrams == 0) return 0.0;
        return 2.0 * commonCount / totalNGrams;
    }

    /**
     * Tạo danh sách n-gram từ chuỗi
     */
    private List<String> generateNGrams(String text, int n) {
        List<String> ngrams = new ArrayList<>();
        if (text == null || text.length() < n) {
            return ngrams;
        }

        for (int i = 0; i <= text.length() - n; i++) {
            ngrams.add(text.substring(i, i + n));
        }

        return ngrams;
    }

    /**
     * Chuẩn hóa văn bản để so sánh
     */
    private String normalizeText(String text) {
        if (text == null) return "";

        // Chuyển sang chữ thường
        String result = text.toLowerCase();

        // Loại bỏ dấu câu không cần thiết
        result = result.replaceAll("[.,;:!?()]", " ");

        // Loại bỏ khoảng trắng thừa
        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }

    /**
     * Trích xuất thực thể từ tin nhắn dựa vào ngữ cảnh và từ khóa
     */
    private Map<String, String> extractEntitiesWithTrainingData(String message, String intent) {
        Map<String, String> entities = new HashMap<>();
        String lowerMessage = message.toLowerCase();

        // Trích xuất ga đi và ga đến
        extractStations(lowerMessage, entities);

        // Trích xuất ngày đi
        extractDepartureDate(lowerMessage, entities);

        // Trích xuất thông tin tàu
        extractTrainInfo(lowerMessage, entities);

        // Trích xuất mã lịch trình
        extractScheduleCode(lowerMessage, entities);

        return entities;
    }

    /**
     * Trích xuất ga đi và ga đến từ tin nhắn
     */
    private void extractStations(String message, Map<String, String> entities) {
        // Tìm ga đi bằng các mẫu regex
        List<Pattern> departurePatterns = Arrays.asList(
                Pattern.compile("từ\\s+ga\\s+([\\p{L}\\s0-9]+?)(?:\\s|đến|tới|đi|$)"),
                Pattern.compile("từ\\s+([\\p{L}\\s0-9]+?)\\s+(?:đến|tới|đi)"),
                Pattern.compile("(?:ga|nhà\\s+ga)\\s+([\\p{L}\\s0-9]+?)\\s+(?:đến|tới|đi)"),
                Pattern.compile("(?:xuất phát|khởi hành|đi)\\s+(?:từ|ở|tại)\\s+([\\p{L}\\s0-9]+?)(?:\\s|đến|tới|đi|$)")
        );

        for (Pattern pattern : departurePatterns) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String stationName = matcher.group(1).trim().toLowerCase();
                // Tìm tên ga chuẩn hóa
                String standardizedName = findStandardStationName(stationName);
                if (standardizedName != null) {
                    entities.put("ga_di", standardizedName);
                    break;
                }
            }
        }

        // Tìm ga đến bằng các mẫu regex
        List<Pattern> arrivalPatterns = Arrays.asList(
                Pattern.compile("(?:đến|tới)\\s+ga\\s+([\\p{L}\\s0-9]+?)(?:\\s|$)"),
                Pattern.compile("(?:đến|tới)\\s+([\\p{L}\\s0-9]+?)(?:\\s|$)"),
                Pattern.compile("(?:ga|nhà\\s+ga)\\s+([\\p{L}\\s0-9]+?)\\s+(?:là\\s+(?:đích|nơi)\\s+(?:đến|tới))"),
                Pattern.compile("đi\\s+([\\p{L}\\s0-9]+?)(?:\\s|$)")
        );

        for (Pattern pattern : arrivalPatterns) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String stationName = matcher.group(1).trim().toLowerCase();
                // Tìm tên ga chuẩn hóa
                String standardizedName = findStandardStationName(stationName);
                if (standardizedName != null) {
                    entities.put("ga_den", standardizedName);
                    break;
                }
            }
        }

        // Nếu không tìm thấy ga đi/đến, thử tìm tên ga để đặt làm ga đi hoặc ga đến
        if (!entities.containsKey("ga_di") && !entities.containsKey("ga_den")) {
            for (String stationKey : STATION_MAP.keySet()) {
                if (message.contains(stationKey)) {
                    // Nếu câu có "đến", "tới" trước tên ga thì là ga đến
                    if (message.matches(".*(?:đến|tới|đi)\\s+.*" + stationKey + ".*")) {
                        entities.put("ga_den", STATION_MAP.get(stationKey));
                    }
                    // Nếu câu có "từ" trước tên ga thì là ga đi
                    else if (message.matches(".*từ\\s+.*" + stationKey + ".*")) {
                        entities.put("ga_di", STATION_MAP.get(stationKey));
                    }
                    // Nếu chỉ tìm thấy 1 ga và không rõ là ga đi hay ga đến
                    else if (!entities.containsKey("ga") && !entities.containsKey("ga_di") && !entities.containsKey("ga_den")) {
                        entities.put("ga", STATION_MAP.get(stationKey));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Tìm tên ga chuẩn hóa từ tên được nhập
     */
    private String findStandardStationName(String inputName) {
        // Tìm trực tiếp
        if (STATION_MAP.containsKey(inputName)) {
            return STATION_MAP.get(inputName);
        }

        // Tìm tương đối (chứa tên)
        for (Map.Entry<String, String> entry : STATION_MAP.entrySet()) {
            if (entry.getKey().contains(inputName) || inputName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Tìm bằng số từ giống nhau
        String[] inputWords = inputName.split("\\s+");
        for (Map.Entry<String, String> entry : STATION_MAP.entrySet()) {
            String[] stationWords = entry.getKey().split("\\s+");
            int matches = 0;
            for (String inputWord : inputWords) {
                for (String stationWord : stationWords) {
                    if (inputWord.equals(stationWord) ||
                            (inputWord.length() > 3 && stationWord.length() > 3 &&
                                    (inputWord.startsWith(stationWord) || stationWord.startsWith(inputWord)))) {
                        matches++;
                        break;
                    }
                }
            }
            // Nếu có ít nhất 1 từ giống nhau và tỷ lệ match >= 0.5
            if (matches > 0 && (double) matches / stationWords.length >= 0.5) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Trích xuất ngày đi
     */
    private void extractDepartureDate(String message, Map<String, String> entities) {
        // Mẫu cho các ngày tương đối
        if (message.contains("hôm nay")) {
            entities.put("ngay_di", "hôm nay");
        } else if (message.contains("ngày mai") || message.contains(" mai")) {
            entities.put("ngay_di", "ngày mai");
        } else if (message.contains("ngày mốt") || message.contains(" mốt")) {
            entities.put("ngay_di", "ngày mốt");
        } else if (message.contains("tuần này")) {
            entities.put("ngay_di", "tuần này");
        } else if (message.contains("tuần sau") || message.contains("tuần tới")) {
            entities.put("ngay_di", "tuần sau");
        } else if (message.contains("cuối tuần") || message.contains("cuối tuần này")) {
            entities.put("ngay_di", "cuối tuần");
        } else if (message.contains("thứ hai") || message.contains("thứ 2")) {
            entities.put("ngay_di", "thứ hai");
        } else if (message.contains("thứ ba") || message.contains("thứ 3")) {
            entities.put("ngay_di", "thứ ba");
        } else if (message.contains("thứ tư") || message.contains("thứ 4")) {
            entities.put("ngay_di", "thứ tư");
        } else if (message.contains("thứ năm") || message.contains("thứ 5")) {
            entities.put("ngay_di", "thứ năm");
        } else if (message.contains("thứ sáu") || message.contains("thứ 6")) {
            entities.put("ngay_di", "thứ sáu");
        } else if (message.contains("thứ bảy") || message.contains("thứ 7")) {
            entities.put("ngay_di", "thứ bảy");
        } else if (message.contains("chủ nhật") || message.contains("cn")) {
            entities.put("ngay_di", "chủ nhật");
        }

        // Mẫu cho ngày cụ thể: dd/mm hoặc dd-mm hoặc dd/mm/yyyy hoặc dd-mm-yyyy
        List<Pattern> datePatterns = Arrays.asList(
                Pattern.compile("(\\d{1,2})[-/](\\d{1,2})(?:[-/](\\d{2,4}))?"),
                Pattern.compile("ngày\\s+(\\d{1,2})\\s+tháng\\s+(\\d{1,2})(?:\\s+năm\\s+(\\d{2,4}))?")
        );

        for (Pattern pattern : datePatterns) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = LocalDate.now().getYear();

                // Nếu có năm
                if (matcher.groupCount() >= 3 && matcher.group(3) != null) {
                    year = Integer.parseInt(matcher.group(3));
                    if (year < 100) {
                        year += 2000; // Giả sử các năm 2 chữ số là thế kỷ 21
                    }
                }

                try {
                    LocalDate date = LocalDate.of(year, month, day);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    entities.put("ngay_di", date.format(formatter));
                    break;
                } catch (DateTimeParseException e) {
                    LOGGER.warning("Không thể chuyển đổi ngày: " + day + "/" + month + "/" + year);
                }
            }
        }
    }

    /**
     * Trích xuất thông tin tàu
     */
    private void extractTrainInfo(String message, Map<String, String> entities) {
        // Mẫu cho tên tàu: SE1, SE2, TN4, LP6, v.v.
        Pattern trainPattern = Pattern.compile("(SE|TN|LP)(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = trainPattern.matcher(message);
        if (matcher.find()) {
            String trainCode = matcher.group(0).toUpperCase();
            entities.put("ten_tau", trainCode);
        }

        // Mẫu cho các loại từ khóa về tàu
        if (!entities.containsKey("ten_tau")) {
            List<Pattern> trainNamePatterns = Arrays.asList(
                    Pattern.compile("tàu\\s+([a-zA-Z0-9]+)"),
                    Pattern.compile("tàu\\s+số\\s+([a-zA-Z0-9]+)")
            );

            for (Pattern pattern : trainNamePatterns) {
                matcher = pattern.matcher(message);
                if (matcher.find()) {
                    String trainCode = matcher.group(1).toUpperCase();
                    if (trainCode.matches("SE\\d+|TN\\d+|LP\\d+")) {
                        entities.put("ten_tau", trainCode);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Trích xuất mã lịch trình
     */
    private void extractScheduleCode(String message, Map<String, String> entities) {
        // Mẫu cho mã lịch trình: LTxxxx hoặc ltxxxx
        Pattern schedulePattern = Pattern.compile("(LT|lt)(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = schedulePattern.matcher(message);
        if (matcher.find()) {
            String scheduleCode = matcher.group(0).toUpperCase();
            entities.put("ma_lich", scheduleCode);
        }

        // Mẫu cho "mã lịch (là) xxxx"
        Pattern idPattern = Pattern.compile("(?:mã|ID)\\s+(?:lịch|lịch trình)(?:\\s+là)?\\s+([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);
        matcher = idPattern.matcher(message);
        if (matcher.find()) {
            String scheduleCode = matcher.group(1).toUpperCase();
            entities.put("ma_lich", scheduleCode);
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
     * Kiểm tra nếu tin nhắn chứa tên ga
     */
    private boolean containsAnyStation(String message) {
        for (String stationKey : STATION_MAP.keySet()) {
            if (message.contains(stationKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra nếu tin nhắn chứa mã tàu/tên tàu
     */
    private boolean containsAnyTrain(String message) {
        Pattern trainPattern = Pattern.compile("(SE|TN|LP)(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = trainPattern.matcher(message);
        return matcher.find();
    }

    /**
     * Xử lý truy vấn về vé tàu
     */
    private String handleTicketQuery(String message, Map<String, String> entities) {
        // Các câu hỏi về giá vé
        if (message.toLowerCase().contains("giá") ||
                message.toLowerCase().contains("bao nhiêu") ||
                message.toLowerCase().contains("chi phí")) {

            String gaDi = entities.getOrDefault("ga_di", null);
            String gaDen = entities.getOrDefault("ga_den", null);
            String tenTau = entities.getOrDefault("ten_tau", null);

            if (gaDi != null && gaDen != null) {
                // Logic tìm kiếm giá vé theo ga đi và ga đến
                StringBuilder response = new StringBuilder();
                response.append("Giá vé tàu từ ").append(gaDi).append(" đến ").append(gaDen).append(" dao động như sau:\n\n");

                if (tenTau != null) {
                    if (tenTau.startsWith("SE")) {
                        response.append("- ").append(tenTau).append(" (Tàu tốc hành):\n")
                                .append("  + Ghế mềm điều hòa: 550.000đ - 750.000đ\n")
                                .append("  + Giường nằm điều hòa: 900.000đ - 1.200.000đ\n\n");
                    } else if (tenTau.startsWith("TN")) {
                        response.append("- ").append(tenTau).append(" (Tàu nhanh):\n")
                                .append("  + Ghế cứng: 350.000đ - 450.000đ\n")
                                .append("  + Ghế mềm điều hòa: 450.000đ - 600.000đ\n")
                                .append("  + Giường nằm điều hòa: 700.000đ - 900.000đ\n\n");
                    } else {
                        response.append("- ").append(tenTau).append(" (Tàu địa phương):\n")
                                .append("  + Ghế cứng: 250.000đ - 350.000đ\n")
                                .append("  + Ghế mềm: 350.000đ - 500.000đ\n\n");
                    }
                } else {
                    response.append("- Tàu tốc hành (SE): 550.000đ - 1.200.000đ\n")
                            .append("- Tàu nhanh (TN): 350.000đ - 900.000đ\n")
                            .append("- Tàu địa phương (LP): 250.000đ - 500.000đ\n\n");
                }

                response.append("Giá vé có thể thay đổi theo thời điểm. Vui lòng kiểm tra giá chính xác khi đặt vé.");
                return response.toString();
            } else if (tenTau != null) {
                return "Giá vé tàu " + tenTau + " dao động từ 250.000đ đến 1.200.000đ tùy theo loại ghế/giường và quãng đường đi. Vui lòng cung cấp ga đi và ga đến để có thông tin giá chính xác hơn.";
            }

            return "Để biết giá vé chính xác, vui lòng cho tôi biết ga đi và ga đến của chuyến tàu bạn quan tâm.";
        }

        // Câu hỏi về đặt vé
        if (message.toLowerCase().contains("đặt vé") ||
                message.toLowerCase().contains("mua vé") ||
                message.toLowerCase().contains("book") ||
                message.toLowerCase().contains("booking")) {

            StringBuilder response = new StringBuilder();
            response.append("Bạn có thể đặt vé tàu bằng các cách sau:\n\n");
            response.append("1. Trực tuyến (Online):\n");
            response.append("   - Website chính thức: www.dsvn.vn\n");
            response.append("   - Ứng dụng di động: DSVN\n");
            response.append("   - Các trang web đối tác: VeXeRe, 12Go.Asia\n\n");
            response.append("2. Trực tiếp:\n");
            response.append("   - Tại các nhà ga trên toàn quốc\n");
            response.append("   - Các đại lý bán vé tàu được ủy quyền\n\n");
            response.append("3. Qua điện thoại:\n");
            response.append("   - Tổng đài đặt vé: 1900 0109\n\n");

            String tenTau = entities.getOrDefault("ten_tau", null);
            if (tenTau != null) {
                response.append("Để đặt vé tàu ").append(tenTau).append(", bạn cần cung cấp thông tin cá nhân, chọn ngày đi, loại ghế/giường và phương thức thanh toán.");
            } else {
                response.append("Khi đặt vé, bạn cần cung cấp thông tin cá nhân, ngày đi, ga đi/đến, loại tàu, loại ghế/giường và phương thức thanh toán.");
            }

            return response.toString();
        }

        // Câu hỏi về đổi/hủy vé
        if (message.toLowerCase().contains("đổi vé") ||
                message.toLowerCase().contains("hủy vé") ||
                message.toLowerCase().contains("hoàn tiền") ||
                message.toLowerCase().contains("hoàn vé") ||
                message.toLowerCase().contains("trả vé")) {

            StringBuilder response = new StringBuilder();
            response.append("Quy định về đổi/hủy vé tàu:\n\n");
            response.append("1. Thời gian:\n");
            response.append("   - Trước 24 giờ so với giờ tàu chạy: hoàn 90% giá vé\n");
            response.append("   - Từ 12-24 giờ so với giờ tàu chạy: hoàn 80% giá vé\n");
            response.append("   - Từ 4-12 giờ so với giờ tàu chạy: hoàn 70% giá vé\n");
            response.append("   - Dưới 4 giờ so với giờ tàu chạy: hoàn 50% giá vé\n\n");
            response.append("2. Thủ tục:\n");
            response.append("   - Vé mua online: Đăng nhập tài khoản và yêu cầu hoàn/hủy\n");
            response.append("   - Vé mua trực tiếp: Mang vé gốc và CMND/CCCD đến ga mua vé\n\n");
            response.append("3. Lưu ý:\n");
            response.append("   - Vé nhóm (từ 10 người trở lên) có quy định riêng\n");
            response.append("   - Vé khuyến mãi có thể không được hoàn/hủy\n");

            return response.toString();
        }

        // Câu hỏi về khuyến mãi
        if (message.toLowerCase().contains("khuyến mãi") ||
                message.toLowerCase().contains("giảm giá") ||
                message.toLowerCase().contains("ưu đãi") ||
                message.toLowerCase().contains("voucher") ||
                message.toLowerCase().contains("coupon")) {

            StringBuilder response = new StringBuilder();
            response.append("Các chương trình khuyến mãi vé tàu hiện có:\n\n");
            response.append("1. Ưu đãi theo đối tượng:\n");
            response.append("   - Học sinh, sinh viên: giảm 10% (cần thẻ học sinh/sinh viên)\n");
            response.append("   - Người cao tuổi (trên 60 tuổi): giảm 15% (cần CMND/CCCD)\n");
            response.append("   - Trẻ em dưới 6 tuổi: miễn phí (không có chỗ ngồi riêng)\n");
            response.append("   - Trẻ em từ 6-10 tuổi: giảm 50% giá vé\n\n");
            response.append("2. Ưu đãi theo thời điểm:\n");
            response.append("   - Đặt vé sớm (trước 30 ngày): giảm đến 15%\n");
            response.append("   - Chương trình \"Tháng vàng khuyến mãi\" (tháng 3, tháng 9): giảm đến 25%\n");
            response.append("   - Chương trình \"Mua chiều đi tặng chiều về\": áp dụng các dịp lễ, Tết\n\n");
            response.append("Vui lòng truy cập website chính thức hoặc liên hệ tổng đài 1900 0109 để biết thêm chi tiết.");

            return response.toString();
        }

        // Trả lời chung
        return selectResponseTemplate("ticket_query", entities);
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
                    String ngayDiStr = entities.get("ngay_di");

                    // Xử lý các ngày tương đối
                    if (ngayDiStr.equals("hôm nay")) {
                        ngayDi = LocalDate.now();
                    } else if (ngayDiStr.equals("ngày mai")) {
                        ngayDi = LocalDate.now().plusDays(1);
                    } else if (ngayDiStr.equals("ngày mốt")) {
                        ngayDi = LocalDate.now().plusDays(2);
                    } else if (ngayDiStr.equals("tuần này")) {
                        // Lấy ngày cuối tuần này (chủ nhật)
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays(7 - dayOfWeek);
                    } else if (ngayDiStr.equals("tuần sau")) {
                        // Lấy ngày đầu tuần sau (thứ 2)
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays(8 - dayOfWeek);
                    } else if (ngayDiStr.equals("cuối tuần")) {
                        // Lấy ngày thứ 7 gần nhất
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays(6 - dayOfWeek);
                        if (ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else if (ngayDiStr.equals("thứ hai")) {
                        // Lấy ngày thứ 2 gần nhất
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays((8 - dayOfWeek) % 7);
                        if (ngayDi.equals(LocalDate.now()) || ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else if (ngayDiStr.equals("thứ ba")) {
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays((9 - dayOfWeek) % 7);
                        if (ngayDi.equals(LocalDate.now()) || ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else if (ngayDiStr.equals("thứ tư")) {
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays((10 - dayOfWeek) % 7);
                        if (ngayDi.equals(LocalDate.now()) || ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else if (ngayDiStr.equals("thứ năm")) {
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays((11 - dayOfWeek) % 7);
                        if (ngayDi.equals(LocalDate.now()) || ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else if (ngayDiStr.equals("thứ sáu")) {
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays((12 - dayOfWeek) % 7);
                        if (ngayDi.equals(LocalDate.now()) || ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else if (ngayDiStr.equals("thứ bảy")) {
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays((13 - dayOfWeek) % 7);
                        if (ngayDi.equals(LocalDate.now()) || ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else if (ngayDiStr.equals("chủ nhật")) {
                        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                        ngayDi = LocalDate.now().plusDays((14 - dayOfWeek) % 7);
                        if (ngayDi.equals(LocalDate.now()) || ngayDi.isBefore(LocalDate.now())) {
                            ngayDi = ngayDi.plusDays(7);
                        }
                    } else {
                        // Giả sử đây là ngày cụ thể định dạng dd/MM/yyyy
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            ngayDi = LocalDate.parse(ngayDiStr, formatter);
                        } catch (Exception e) {
                            LOGGER.warning("Không thể chuyển đổi ngày: " + ngayDiStr);
                        }
                    }

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
                            .append(" - Tàu: ").append(lichTrinh.getTau().getTenTau())
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
            String maTau = entities.getOrDefault("ten_tau", null);
            String maLich = entities.getOrDefault("ma_lich", null);

            // Nếu không tìm thấy thì thử trích xuất từ tin nhắn
            if (maTau == null && maLich == null) {
                maTau = extractTrainName(message);
                if (maTau == null) {
                    // Tìm mã lịch
                    Pattern schedulePattern = Pattern.compile("(LT|lt)(\\d+)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = schedulePattern.matcher(message);
                    if (matcher.find()) {
                        maLich = matcher.group(0).toUpperCase();
                    } else {
                        // Tìm số lịch trình
                        Pattern numberPattern = Pattern.compile("\\b\\d+\\b");
                        matcher = numberPattern.matcher(message);
                        if (matcher.find()) {
                            maLich = "LT" + matcher.group(0);
                        }
                    }
                }
            }

            // Nếu vẫn không tìm thấy, kiểm tra nếu có ga đi và ga đến, xử lý theo trạng thái lịch trình
            if (maTau == null && maLich == null) {
                if (entities.containsKey("ga_di") && entities.containsKey("ga_den")) {
                    String gaDi = entities.get("ga_di");
                    String gaDen = entities.get("ga_den");

                    // Có thêm ngày không?
                    String ngayDi = entities.getOrDefault("ngay_di", null);
                    LocalDate date = null;

                    if (ngayDi != null) {
                        if (ngayDi.equals("hôm nay")) {
                            date = LocalDate.now();
                        } else if (ngayDi.equals("ngày mai")) {
                            date = LocalDate.now().plusDays(1);
                        } else {
                            // Thử chuyển từ chuỗi sang ngày
                            try {
                                date = LocalDate.parse(ngayDi, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            } catch (Exception e) {
                                LOGGER.fine("Không chuyển đổi được ngày: " + ngayDi);
                            }
                        }
                    }

                    return handleRouteStatusQuery(gaDi, gaDen, date);
                }

                return "Bạn có thể cho tôi biết mã tàu (ví dụ: SE2, TN4) hoặc mã lịch trình (ví dụ: LT1234) cụ thể để kiểm tra trạng thái không?";
            }

            // Kiểm tra xem đây là mã lịch hay mã tàu
            if (maTau != null) {
                return handleTrainStatusQuery(maTau);
            } else {
                return handleScheduleStatusQuery(maLich);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn trạng thái: " + e.getMessage(), e);
            return "Xin lỗi, tôi không thể kiểm tra trạng thái vào lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Trích xuất tên tàu từ tin nhắn
     * @param message Tin nhắn cần trích xuất
     * @return Tên tàu được trích xuất hoặc null nếu không tìm thấy
     */
    private String extractTrainName(String message) {
        message = message.toLowerCase();

        // Pattern 1: Tìm kiếm mẫu tên tàu phổ biến (SE2, TN4, LP6, v.v.)
        Pattern trainNamePattern = Pattern.compile("(se|tn|lp)[0-9]+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = trainNamePattern.matcher(message);

        if (matcher.find()) {
            return matcher.group().toUpperCase();
        }

        // Pattern 2: Tìm kiếm theo từ khóa "tàu" + số hiệu
        Pattern trainKeywordPattern = Pattern.compile("tàu\\s+([a-zA-Z]{2}[0-9]+)", Pattern.CASE_INSENSITIVE);
        matcher = trainKeywordPattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }

        // Pattern 3: Tìm kiếm theo từ khóa "tàu số" + số hiệu
        Pattern trainNumberPattern = Pattern.compile("tàu\\s+(?:số)?\\s+([a-zA-Z]{2}[0-9]+)", Pattern.CASE_INSENSITIVE);
        matcher = trainNumberPattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }

        // Pattern 4: Tìm kiếm các từ khóa kèm với loại tàu
        Map<String, String> trainTypes = new HashMap<>();
        trainTypes.put("tốc hành", "SE");
        trainTypes.put("tàu nhanh", "TN");
        trainTypes.put("địa phương", "LP");

        for (Map.Entry<String, String> entry : trainTypes.entrySet()) {
            Pattern typeNumberPattern = Pattern.compile(entry.getKey() + "\\s+(?:số)?\\s*([0-9]+)", Pattern.CASE_INSENSITIVE);
            matcher = typeNumberPattern.matcher(message);

            if (matcher.find()) {
                return entry.getValue() + matcher.group(1);
            }
        }

        // Pattern 5: Tìm kiếm riêng các mã như SE, TN, LP kèm số
        for (String prefix : new String[]{"se", "tn", "lp"}) {
            Pattern prefixPattern = Pattern.compile("\\b" + prefix + "\\s*([0-9]+)\\b", Pattern.CASE_INSENSITIVE);
            matcher = prefixPattern.matcher(message);

            if (matcher.find()) {
                return prefix.toUpperCase() + matcher.group(1);
            }
        }

        return null;
    }

    /**
     * Xử lý truy vấn trạng thái tuyến đường
     */
    private String handleRouteStatusQuery(String gaDi, String gaDen, LocalDate ngayDi) {
        try {
            List<LichTrinhTau> lichTrinhs = searchSchedules(gaDi, gaDen, ngayDi);

            if (lichTrinhs.isEmpty()) {
                String response = "Không tìm thấy lịch trình tàu nào ";

                if (gaDi != null) response += "từ " + gaDi + " ";
                if (gaDen != null) response += "đến " + gaDen + " ";
                if (ngayDi != null) response += "vào ngày " + ngayDi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " ";

                return response;
            }

            // Thống kê trạng thái
            int daKhoiHanh = 0;
            int chuaKhoiHanh = 0;
            int daHuy = 0;

            for (LichTrinhTau lichTrinh : lichTrinhs) {
                if (lichTrinh.getTrangThai() == TrangThai.DA_KHOI_HANH) {
                    daKhoiHanh++;
                } else if (lichTrinh.getTrangThai() == TrangThai.CHUA_KHOI_HANH) {
                    chuaKhoiHanh++;
                } else if (lichTrinh.getTrangThai() == TrangThai.DA_HUY) {
                    daHuy++;
                }
            }

            StringBuilder response = new StringBuilder();
            response.append("Thông tin trạng thái tàu từ ").append(gaDi).append(" đến ").append(gaDen);

            if (ngayDi != null) {
                response.append(" ngày ").append(ngayDi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            response.append(":\n\n");
            response.append("- Tổng số lịch trình: ").append(lichTrinhs.size()).append("\n");
            response.append("- Đã khởi hành: ").append(daKhoiHanh).append("\n");
            response.append("- Chưa khởi hành: ").append(chuaKhoiHanh).append("\n");
            response.append("- Đã hủy: ").append(daHuy).append("\n\n");

            // Hiển thị chi tiết 2 lịch trình gần nhất
            if (!lichTrinhs.isEmpty()) {
                lichTrinhs.sort(Comparator.comparing(LichTrinhTau::getNgayDi)
                        .thenComparing(LichTrinhTau::getGioDi));

                int count = Math.min(lichTrinhs.size(), 2);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                response.append("Thông tin chi tiết:\n");

                for (int i = 0; i < count; i++) {
                    LichTrinhTau lichTrinh = lichTrinhs.get(i);
                    response.append(i + 1).append(". Mã lịch: ").append(lichTrinh.getMaLich())
                            .append(" - Tàu: ").append(lichTrinh.getTau().getTenTau())
                            .append(" - Ngày đi: ").append(lichTrinh.getNgayDi().format(dateFormatter))
                            .append(" - Giờ đi: ").append(lichTrinh.getGioDi().format(timeFormatter))
                            .append(" - Trạng thái: ").append(lichTrinh.getTrangThai().getValue())
                            .append("\n");
                }
            }
            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý trạng thái tuyến đường: " + e.getMessage(), e);
            return "Xin lỗi, tôi không thể kiểm tra trạng thái tuyến đường vào lúc này. Vui lòng thử lại sau.";
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
                // Tìm lịch trình sắp tới của tàu này
                List<LichTrinhTau> upcomingSchedules = new ArrayList<>();
                LocalDate today = LocalDate.now();

                for (LichTrinhTau lichTrinh : trainSchedules) {
                    if (lichTrinh.getTrangThai() == TrangThai.CHUA_KHOI_HANH &&
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

                if (!upcomingSchedules.isEmpty()) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                    LichTrinhTau nextSchedule = upcomingSchedules.get(0);
                    response.append("Lịch trình sắp tới:\n");
                    response.append("Mã lịch: ").append(nextSchedule.getMaLich()).append("\n");
                    response.append("Tuyến: ").append(nextSchedule.getTau().getTuyenTau().getGaDi())
                            .append(" → ").append(nextSchedule.getTau().getTuyenTau().getGaDen()).append("\n");
                    response.append("Ngày đi: ").append(nextSchedule.getNgayDi().format(dateFormatter)).append("\n");
                    response.append("Giờ đi: ").append(nextSchedule.getGioDi().format(timeFormatter)).append("\n");

                    // Thêm thông tin về ga hiện tại nếu tàu đã khởi hành
                    if (nextSchedule.getTrangThai() == TrangThai.DA_KHOI_HANH) {
                        response.append("Tàu đã khởi hành lúc ").append(nextSchedule.getGioDi().format(timeFormatter)).append("\n");

                        // Thêm thời gian đến dự kiến bằng AI
                        try {
                            AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(nextSchedule);
                            response.append("Giờ đến dự kiến: ")
                                    .append(prediction.getEstimatedArrivalTime(nextSchedule.getGioDi()).format(timeFormatter));
                        } catch (Exception e) {
                            response.append("Giờ đến: Đang cập nhật");
                        }
                    } else {
                        response.append("Trạng thái: ").append(nextSchedule.getTrangThai().getValue());
                    }
                }
            } else if (daKhoiHanh > 0) {
                // Tìm lịch trình đã khởi hành gần đây nhất
                List<LichTrinhTau> recentSchedules = new ArrayList<>();
                for (LichTrinhTau lichTrinh : trainSchedules) {
                    if (lichTrinh.getTrangThai() == TrangThai.DA_KHOI_HANH) {
                        recentSchedules.add(lichTrinh);
                    }
                }

                // Sắp xếp theo ngày và giờ đi
                recentSchedules.sort((a, b) -> {
                    int dateCompare = b.getNgayDi().compareTo(a.getNgayDi()); // Đảo ngược để lấy gần đây nhất
                    if (dateCompare != 0) return dateCompare;
                    return b.getGioDi().compareTo(a.getGioDi());
                });

                if (!recentSchedules.isEmpty()) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                    LichTrinhTau recentSchedule = recentSchedules.get(0);
                    response.append("Lịch trình gần đây nhất:\n");
                    response.append("Mã lịch: ").append(recentSchedule.getMaLich()).append("\n");
                    response.append("Tuyến: ").append(recentSchedule.getTau().getTuyenTau().getGaDi())
                            .append(" → ").append(recentSchedule.getTau().getTuyenTau().getGaDen()).append("\n");
                    response.append("Ngày đi: ").append(recentSchedule.getNgayDi().format(dateFormatter)).append("\n");
                    response.append("Giờ đi: ").append(recentSchedule.getGioDi().format(timeFormatter)).append("\n");
                    response.append("Trạng thái: ").append(recentSchedule.getTrangThai().getValue());

                    // Thêm thời gian đến dự kiến bằng AI
                    try {
                        AITravelTimePredictor.PredictionResult prediction = aiPredictor.predictTravelTime(recentSchedule);
                        response.append("\nGiờ đến dự kiến: ")
                                .append(prediction.getEstimatedArrivalTime(recentSchedule.getGioDi()).format(timeFormatter));
                    } catch (Exception e) {
                        response.append("\nGiờ đến: Đang cập nhật");
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
            response.append("- Tàu: ").append(lichTrinh.getTau().getTenTau()).append("\n");
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

            // Thêm thông tin thêm tùy theo trạng thái
            if (lichTrinh.getTrangThai() == TrangThai.DA_KHOI_HANH) {
                response.append("\nTàu đã khởi hành lúc ").append(lichTrinh.getGioDi().format(timeFormatter));
                response.append(" ngày ").append(lichTrinh.getNgayDi().format(dateFormatter)).append(".");
            } else if (lichTrinh.getTrangThai() == TrangThai.CHUA_KHOI_HANH) {
                response.append("\nTàu sẽ khởi hành lúc ").append(lichTrinh.getGioDi().format(timeFormatter));
                response.append(" ngày ").append(lichTrinh.getNgayDi().format(dateFormatter)).append(".");

                // Thêm thông tin về số vé còn trống nếu có
                int soVeDaDat = countBookedTickets(lichTrinh.getMaLich());
                int tongSoVe = calculateTotalSeats(lichTrinh.getTau());

                if (tongSoVe > 0) {
                    int veConTrong = tongSoVe - soVeDaDat;
                    response.append("\nSố vé còn trống: ").append(veConTrong).append("/").append(tongSoVe);
                }
            } else if (lichTrinh.getTrangThai() == TrangThai.DA_HUY) {
                response.append("\nChuyến tàu này đã bị hủy. Vui lòng liên hệ nhà ga để biết thêm thông tin.");
            }

            return response.toString();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi xử lý truy vấn trạng thái lịch trình: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Đếm số vé đã đặt cho một lịch trình
     */
    private int countBookedTickets(String maLich) {
        try {
            // Logic đếm vé đã đặt
            // Giả định là phương thức tối nay thực hiện truy vấn đến CSDL
            // Trong tình huống thực tế, bạn cần thay thế đoạn code này bằng logic thực tế
            return 30; // Giá trị giả định cho mục đích demo
        } catch (Exception e) {
            LOGGER.warning("Lỗi khi đếm vé đã đặt: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Tính tổng số chỗ của một tàu
     */
    private int calculateTotalSeats(Tau tau) {
        try {
            // Logic tính tổng số chỗ ngồi/giường
            // Giả định là dựa vào số toa và loại toa
            int soToa = tau.getSoToa();
            // Giả sử mỗi toa có trung bình 40 chỗ
            return soToa * 40;
        } catch (Exception e) {
            LOGGER.warning("Lỗi khi tính tổng số chỗ: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Xử lý truy vấn về ga tàu
     */
    private String handleStationQuery(String message, Map<String, String> entities) {
        // Thông tin ga tàu cơ bản
        Map<String, String[]> stationInfo = new HashMap<>();
        stationInfo.put("hà nội", new String[] {
                "Ga Hà Nội",
                "120 Lê Duẩn, Khâm Thiên, Đống Đa, Hà Nội",
                "024.3825.3949",
                "Ga Hà Nội là ga trung tâm lớn nhất miền Bắc, có các tàu đi các tỉnh phía Nam và các tỉnh phía Bắc. Ga có đầy đủ các dịch vụ như phòng chờ VIP, dịch vụ ăn uống, giữ hành lý, taxi nội thành."
        });
        stationInfo.put("sài gòn", new String[] {
                "Ga Sài Gòn",
                "1 Nguyễn Thông, Phường 9, Quận 3, TP. Hồ Chí Minh",
                "028.3846.6119",
                "Ga Sài Gòn là ga đường sắt lớn nhất miền Nam, điểm cuối của tuyến đường sắt Bắc Nam. Ga có các dịch vụ hiện đại như phòng chờ máy lạnh, dịch vụ hỗ trợ hành khách, khu vực dịch vụ ăn uống."
        });
        stationInfo.put("đà nẵng", new String[] {
                "Ga Đà Nẵng",
                "791 Hải Phòng, Tam Thuận, Thanh Khê, Đà Nẵng",
                "0236.3827.070",
                "Ga Đà Nẵng nằm ở trung tâm thành phố Đà Nẵng, là ga trung gian quan trọng của tuyến đường sắt Bắc Nam. Ga có các dịch vụ hỗ trợ du lịch, dịch vụ đặt taxi và xe khách đến các điểm du lịch."
        });
        stationInfo.put("huế", new String[] {
                "Ga Huế",
                "2 Bùi Thị Xuân, Phường Đúc, Thành phố Huế",
                "0234.3822.175",
                "Ga Huế là ga chính của thành phố Huế, nằm gần trung tâm thành phố. Ga có kiến trúc đặc trưng của thời Pháp và các dịch vụ hỗ trợ tham quan các điểm du lịch của cố đô Huế."
        });
        stationInfo.put("nha trang", new String[] {
                "Ga Nha Trang",
                "17 Thái Nguyên, Phước Tân, Nha Trang, Khánh Hòa",
                "0258.3822.113",
                "Ga Nha Trang nằm ở trung tâm thành phố biển Nha Trang, cách bãi biển chỉ khoảng 1km. Ga có các dịch vụ hướng dẫn du lịch, đặt tour và dịch vụ đưa đón khách sạn."
        });

        // Tìm ga tàu trong entities
        String station = entities.getOrDefault("ga", null);

        // Nếu không tìm thấy trong entities, thử tìm trong các key của stationInfo
        if (station == null) {
            String messageLower = message.toLowerCase();
            for (String stationKey : STATION_MAP.keySet()) {
                if (messageLower.contains(stationKey)) {
                    station = STATION_MAP.get(stationKey);
                    break;
                }
            }
        }

        if (station != null) {
            // Chuyển đổi tên ga sang dạng key tương ứng trong map
            String stationKey = station.toLowerCase();

            // Tìm key tương ứng cho stationInfo
            String matchingKey = null;
            for (String key : stationInfo.keySet()) {
                if (key.equals(stationKey) || stationKey.contains(key) || key.contains(stationKey)) {
                    matchingKey = key;
                    break;
                }
            }

            if (matchingKey != null) {
                String[] info = stationInfo.get(matchingKey);
                StringBuilder response = new StringBuilder();
                response.append("Thông tin về ").append(info[0]).append(":\n\n");
                response.append("- Địa chỉ: ").append(info[1]).append("\n");
                response.append("- Điện thoại: ").append(info[2]).append("\n\n");

                // Thêm thông tin chi tiết nếu có
                if (info.length > 3) {
                    response.append(info[3]).append("\n\n");
                }

                // Thêm câu hỏi gợi ý
                response.append("Bạn cần tìm lịch trình tàu từ ga này không?");

                return response.toString();
            }
        }

        return "Bạn muốn biết thông tin về ga tàu nào? Tôi có thông tin về các ga chính như Hà Nội, Sài Gòn, Đà Nẵng, Huế và Nha Trang.";
    }

    /**
     * Xử lý truy vấn về tàu
     */
    private String handleTrainQuery(String message, Map<String, String> entities) {
        try {
            // Tìm ID tàu trong entities
            String trainId = entities.getOrDefault("ten_tau", null);

            // Nếu không tìm thấy trong entities, thử trích xuất từ tin nhắn
            if (trainId == null) {
                trainId = extractTrainName(message);
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
            response.append("Thông tin về tàu ").append(train.getTenTau()).append(" (").append(train.getMaTau()).append("):\n\n");
            response.append("- Tuyến: ").append(train.getTuyenTau().getGaDi())
                    .append(" → ").append(train.getTuyenTau().getGaDen()).append("\n");
            response.append("- Số toa: ").append(train.getSoToa()).append("\n");

            // Thêm thông tin về toa theo loại nếu có
            if (train.getDanhSachToaTau() != null && !train.getDanhSachToaTau().isEmpty()) {
                Map<String, Integer> loaiToaCount = new HashMap<>();
                for (ToaTau toa : train.getDanhSachToaTau()) {
                    String loaiToa = toa.getClass().getSimpleName();
                    loaiToaCount.put(loaiToa, loaiToaCount.getOrDefault(loaiToa, 0) + 1);
                }

                response.append("- Thông tin toa:\n");
                for (Map.Entry<String, Integer> entry : loaiToaCount.entrySet()) {
                    response.append("  + ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" toa\n");
                }
            }

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
            } else {
                response.append("\nHiện tại không có lịch trình sắp tới của tàu này.");
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
        if (!isHelpful || intent.equals("fallback")) {
            // Nếu phản hồi không hữu ích, lưu lại để phân tích và cải thiện sau này
            try {
                File feedbackDir = new File("feedback_data");
                if (!feedbackDir.exists()) {
                    feedbackDir.mkdirs();
                }

                File feedbackFile = new File("feedback_data/feedback_log.txt");
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
     * Viết hoa chữ cái đầu
     */
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Xử lý tin nhắn chào hỏi
     */
    private String handleGreeting(String message) {
        return selectResponseTemplate("greeting", new HashMap<>());
    }

    /**
     * Xử lý tin nhắn cảm ơn
     */
    private String handleThanks() {
        return selectResponseTemplate("thanks", new HashMap<>());
    }

    /**
     * Xử lý tin nhắn tạm biệt
     */
    private String handleGoodbye() {
        return selectResponseTemplate("goodbye", new HashMap<>());
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
     * Tìm kiếm lịch trình theo mã tàu
     */
    private List<LichTrinhTau> findSchedulesByTrainId(String trainId) {
        try {
            List<LichTrinhTau> allSchedules = lichTrinhTauDAO.getAllList();
            List<LichTrinhTau> filteredSchedules = new ArrayList<>();

            for (LichTrinhTau lichTrinh : allSchedules) {
                if (lichTrinh.getTau().getMaTau().equalsIgnoreCase(trainId) ||
                        lichTrinh.getTau().getTenTau().equalsIgnoreCase(trainId)) {
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
                if (lichTrinh.getTau().getMaTau().equalsIgnoreCase(trainId) ||
                        lichTrinh.getTau().getTenTau().equalsIgnoreCase(trainId)) {
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
        StringBuilder response = new StringBuilder();
        response.append("Thông tin chung về các loại tàu:\n\n");
        response.append("1. Tàu SE (Tốc hành/Express):\n");
        response.append("   - Tàu cao cấp nhất, tốc độ nhanh và ít dừng\n");
        response.append("   - Có đầy đủ các loại toa: giường nằm điều hòa, ghế ngồi mềm điều hòa\n");
        response.append("   - Các tàu phổ biến: SE1, SE2, SE3, SE4, SE5, SE6\n\n");

        response.append("2. Tàu TN (Tàu nhanh):\n");
        response.append("   - Tàu nhanh, dừng tại các ga chính\n");
        response.append("   - Có các loại toa: giường nằm, ghế ngồi mềm, ghế ngồi cứng\n");
        response.append("   - Các tàu phổ biến: TN1, TN2, TN3, TN4\n\n");

        response.append("3. Tàu LP (Địa phương):\n");
        response.append("   - Tàu chạy các tuyến ngắn, dừng tại nhiều ga\n");
        response.append("   - Chủ yếu có ghế ngồi cứng và ghế ngồi mềm\n");
        response.append("   - Các tàu phổ biến: LP1, LP2, LP3, LP6, LP8\n\n");

        response.append("Để tìm thông tin về một tàu cụ thể, vui lòng nêu rõ mã tàu (Ví dụ: \"Thông tin về tàu SE2\")");

        return response.toString();
    }

    /**
     * Lấy một phản hồi ngẫu nhiên từ danh sách các phản hồi có sẵn
     */
    private String getRandomResponse(String[] responses) {
        int index = (int) (Math.random() * responses.length);
        return responses[index];
    }

    /**
     * Chọn mẫu câu trả lời cho intent và thay thế các biến
     */
    private String selectResponseTemplate(String intent, Map<String, String> entities) {
        List<String> templates = responseTemplates.getOrDefault(intent, responseTemplates.get("fallback"));

        if (templates == null || templates.isEmpty()) {
            return "Xin lỗi, tôi không thể trả lời câu hỏi này.";
        }

        // Chọn ngẫu nhiên một mẫu câu trả lời
        int randomIndex = new Random().nextInt(templates.size());
        String template = templates.get(randomIndex);

        // Thay thế các biến trong template
        for (Map.Entry<String, String> entry : entities.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (template.contains(placeholder)) {
                template = template.replace(placeholder, entry.getValue());
            }
        }

        // Xóa các placeholder chưa được thay thế
        template = template.replaceAll("\\{[^\\}]*\\}", "");

        return template;
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
            greetingQueries.put(new JSONObject().put("query", "Hi bot").put("entities", new JSONObject()));
            greetingQueries.put(new JSONObject().put("query", "Này trợ lý").put("entities", new JSONObject()));
            intents.put("greeting", greetingQueries);

            // Thanks intent
            JSONArray thanksQueries = new JSONArray();
            thanksQueries.put(new JSONObject().put("query", "Cảm ơn").put("entities", new JSONObject()));
            thanksQueries.put(new JSONObject().put("query", "Cám ơn bạn nhiều").put("entities", new JSONObject()));
            thanksQueries.put(new JSONObject().put("query", "Thank you").put("entities", new JSONObject()));
            thanksQueries.put(new JSONObject().put("query", "Cảm ơn vì thông tin").put("entities", new JSONObject()));
            thanksQueries.put(new JSONObject().put("query", "Cám ơn trợ lý").put("entities", new JSONObject()));
            intents.put("thanks", thanksQueries);

            // Goodbye intent
            JSONArray goodbyeQueries = new JSONArray();
            goodbyeQueries.put(new JSONObject().put("query", "Tạm biệt").put("entities", new JSONObject()));
            goodbyeQueries.put(new JSONObject().put("query", "Bye").put("entities", new JSONObject()));
            goodbyeQueries.put(new JSONObject().put("query", "Hẹn gặp lại").put("entities", new JSONObject()));
            goodbyeQueries.put(new JSONObject().put("query", "Tôi đi đây").put("entities", new JSONObject()));
            goodbyeQueries.put(new JSONObject().put("query", "Chào tạm biệt").put("entities", new JSONObject()));
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

            JSONObject query3 = new JSONObject();
            query3.put("query", "Kiểm tra lịch trình tàu từ Vinh đến Huế");
            JSONObject entities3 = new JSONObject();
            entities3.put("ga_di", "Vinh");
            entities3.put("ga_den", "Huế");
            query3.put("entities", entities3);
            scheduleQueries.put(query3);

            JSONObject query4 = new JSONObject();
            query4.put("query", "Tàu từ Hải Phòng đến Hà Nội có những chuyến nào");
            JSONObject entities4 = new JSONObject();
            entities4.put("ga_di", "Hải Phòng");
            entities4.put("ga_den", "Hà Nội");
            query4.put("entities", entities4);
            scheduleQueries.put(query4);

            JSONObject query5 = new JSONObject();
            query5.put("query", "Lịch trình tàu từ Đà Nẵng đi Nha Trang ngày 20/5");
            JSONObject entities5 = new JSONObject();
            entities5.put("ga_di", "Đà Nẵng");
            entities5.put("ga_den", "Nha Trang");
            entities5.put("ngay_di", "20/5");
            query5.put("entities", entities5);
            scheduleQueries.put(query5);

            intents.put("schedule_query", scheduleQueries);

            // Status query intent
            JSONArray statusQueries = new JSONArray();

            JSONObject statusQuery1 = new JSONObject();
            statusQuery1.put("query", "Trạng thái của tàu SE2");
            JSONObject statusEntities1 = new JSONObject();
            statusEntities1.put("ten_tau", "SE2");
            statusQuery1.put("entities", statusEntities1);
            statusQueries.put(statusQuery1);

            JSONObject statusQuery2 = new JSONObject();
            statusQuery2.put("query", "Tàu SE3 đã khởi hành chưa");
            JSONObject statusEntities2 = new JSONObject();
            statusEntities2.put("ten_tau", "SE3");
            statusQuery2.put("entities", statusEntities2);
            statusQueries.put(statusQuery2);

            JSONObject statusQuery3 = new JSONObject();
            statusQuery3.put("query", "Kiểm tra lịch trình LT0012 đã đi chưa");
            JSONObject statusEntities3 = new JSONObject();
            statusEntities3.put("ma_lich", "LT0012");
            statusQuery3.put("entities", statusEntities3);
            statusQueries.put(statusQuery3);

            JSONObject statusQuery4 = new JSONObject();
            statusQuery4.put("query", "Tàu TN5 có bị hủy không");
            JSONObject statusEntities4 = new JSONObject();
            statusEntities4.put("ten_tau", "TN5");
            statusQuery4.put("entities", statusEntities4);
            statusQueries.put(statusQuery4);

            JSONObject statusQuery5 = new JSONObject();
            statusQuery5.put("query", "Tình trạng chuyến tàu từ Hà Nội đi Sài Gòn hôm nay");
            JSONObject statusEntities5 = new JSONObject();
            statusEntities5.put("ga_di", "Hà Nội");
            statusEntities5.put("ga_den", "Sài Gòn");
            statusEntities5.put("ngay_di", "hôm nay");
            statusQuery5.put("entities", statusEntities5);
            statusQueries.put(statusQuery5);

            intents.put("status_query", statusQueries);

            // Station query intent
            JSONArray stationQueries = new JSONArray();

            JSONObject stationQuery1 = new JSONObject();
            stationQuery1.put("query", "Thông tin về ga Hà Nội");
            JSONObject stationEntities1 = new JSONObject();
            stationEntities1.put("ga", "Hà Nội");
            stationQuery1.put("entities", stationEntities1);
            stationQueries.put(stationQuery1);

            JSONObject stationQuery2 = new JSONObject();
            stationQuery2.put("query", "Cho tôi địa chỉ ga Sài Gòn");
            JSONObject stationEntities2 = new JSONObject();
            stationEntities2.put("ga", "Sài Gòn");
            stationQuery2.put("entities", stationEntities2);
            stationQueries.put(stationQuery2);

            JSONObject stationQuery3 = new JSONObject();
            stationQuery3.put("query", "Ga Đà Nẵng ở đâu");
            JSONObject stationEntities3 = new JSONObject();
            stationEntities3.put("ga", "Đà Nẵng");
            stationQuery3.put("entities", stationEntities3);
            stationQueries.put(stationQuery3);

            JSONObject stationQuery4 = new JSONObject();
            stationQuery4.put("query", "Số điện thoại của ga Huế");
            JSONObject stationEntities4 = new JSONObject();
            stationEntities4.put("ga", "Huế");
            stationQuery4.put("entities", stationEntities4);
            stationQueries.put(stationQuery4);

            JSONObject stationQuery5 = new JSONObject();
            stationQuery5.put("query", "Ga Nha Trang có những tiện ích gì");
            JSONObject stationEntities5 = new JSONObject();
            stationEntities5.put("ga", "Nha Trang");
            stationQuery5.put("entities", stationEntities5);
            stationQueries.put(stationQuery5);

            intents.put("station_query", stationQueries);

            // Train query intent
            JSONArray trainQueries = new JSONArray();

            JSONObject trainQuery1 = new JSONObject();
            trainQuery1.put("query", "Thông tin về tàu SE2");
            JSONObject trainEntities1 = new JSONObject();
            trainEntities1.put("ten_tau", "SE2");
            trainQuery1.put("entities", trainEntities1);
            trainQueries.put(trainQuery1);

            JSONObject trainQuery2 = new JSONObject();
            trainQuery2.put("query", "Tàu TN4 có chạy tuyến nào");
            JSONObject trainEntities2 = new JSONObject();
            trainEntities2.put("ten_tau", "TN4");
            trainQuery2.put("entities", trainEntities2);
            trainQueries.put(trainQuery2);

            JSONObject trainQuery3 = new JSONObject();
            trainQuery3.put("query", "Cho tôi biết về tàu LP6");
            JSONObject trainEntities3 = new JSONObject();
            trainEntities3.put("ten_tau", "LP6");
            trainQuery3.put("entities", trainEntities3);
            trainQueries.put(trainQuery3);

            JSONObject trainQuery4 = new JSONObject();
            trainQuery4.put("query", "Tàu SE1 có bao nhiêu toa");
            JSONObject trainEntities4 = new JSONObject();
            trainEntities4.put("ten_tau", "SE1");
            trainQuery4.put("entities", trainEntities4);
            trainQueries.put(trainQuery4);

            JSONObject trainQuery5 = new JSONObject();
            trainQuery5.put("query", "Lịch trình sắp tới của tàu SE5");
            JSONObject trainEntities5 = new JSONObject();
            trainEntities5.put("ten_tau", "SE5");
            trainQuery5.put("entities", trainEntities5);
            trainQueries.put(trainQuery5);

            intents.put("train_query", trainQueries);

            // Ticket query intent
            JSONArray ticketQueries = new JSONArray();

            JSONObject ticketQuery1 = new JSONObject();
            ticketQuery1.put("query", "Giá vé tàu từ Hà Nội đến Đà Nẵng");
            JSONObject ticketEntities1 = new JSONObject();
            ticketEntities1.put("ga_di", "Hà Nội");
            ticketEntities1.put("ga_den", "Đà Nẵng");
            ticketQuery1.put("entities", ticketEntities1);
            ticketQueries.put(ticketQuery1);

            JSONObject ticketQuery2 = new JSONObject();
            ticketQuery2.put("query", "Có thể đặt vé tàu SE2 như thế nào");
            JSONObject ticketEntities2 = new JSONObject();
            ticketEntities2.put("ten_tau", "SE2");
            ticketQuery2.put("entities", ticketEntities2);
            ticketQueries.put(ticketQuery2);

            JSONObject ticketQuery3 = new JSONObject();
            ticketQuery3.put("query", "Quy định đổi vé tàu thế nào");
            ticketQuery3.put("entities", new JSONObject());
            ticketQueries.put(ticketQuery3);

            JSONObject ticketQuery4 = new JSONObject();
            ticketQuery4.put("query", "Làm sao để hủy vé đã đặt");
            ticketQuery4.put("entities", new JSONObject());
            ticketQueries.put(ticketQuery4);

            JSONObject ticketQuery5 = new JSONObject();
            ticketQuery5.put("query", "Khuyến mãi vé tàu dịp lễ");
            ticketQuery5.put("entities", new JSONObject());
            ticketQueries.put(ticketQuery5);

            intents.put("ticket_query", ticketQueries);

            trainingData.put("intents", intents);

            // Tạo responses
            JSONObject responses = new JSONObject();

            // Greeting responses
            JSONArray greetingResponses = new JSONArray();
            greetingResponses.put("Xin chào! Tôi có thể giúp gì cho bạn về lịch trình tàu hỏa?");
            greetingResponses.put("Chào bạn! Bạn cần tìm thông tin gì về lịch trình tàu?");
            greetingResponses.put("Xin chào! Tôi là trợ lý ảo. Bạn cần hỗ trợ thông tin gì về tàu hỏa?");
            responses.put("greeting", greetingResponses);

            // Thanks responses
            JSONArray thanksResponses = new JSONArray();
            thanksResponses.put("Không có gì! Rất vui khi được giúp bạn.");
            thanksResponses.put("Không có chi! Bạn cần hỗ trợ gì thêm không?");
            thanksResponses.put("Rất vui khi được hỗ trợ bạn. Bạn còn câu hỏi nào khác không?");
            responses.put("thanks", thanksResponses);

            // Goodbye responses
            JSONArray goodbyeResponses = new JSONArray();
            goodbyeResponses.put("Tạm biệt! Rất vui được hỗ trợ bạn.");
            goodbyeResponses.put("Chúc bạn một ngày tốt lành! Hẹn gặp lại.");
            goodbyeResponses.put("Tạm biệt và hẹn gặp lại. Khi cần thông tin về lịch trình tàu, hãy quay lại nhé!");
            responses.put("goodbye", goodbyeResponses);

            // Schedule query responses
            JSONArray scheduleResponses = new JSONArray();
            scheduleResponses.put("Đây là lịch trình tàu từ {ga_di} đến {ga_den} mà bạn yêu cầu.");
            scheduleResponses.put("Tôi đã tìm thấy các lịch trình sau từ {ga_di} đến {ga_den}.");
            scheduleResponses.put("Dưới đây là thông tin lịch trình tàu từ {ga_di} đến {ga_den} mà bạn cần.");
            responses.put("schedule_query", scheduleResponses);

            // Status query responses
            JSONArray statusResponses = new JSONArray();
            statusResponses.put("Thông tin trạng thái của tàu {ten_tau} hiện tại là: ");
            statusResponses.put("Tàu {ten_tau} hiện đang ở trạng thái: ");
            statusResponses.put("Tình trạng hiện tại của tàu {ten_tau}: ");
            responses.put("status_query", statusResponses);

            // Station query responses
            JSONArray stationResponses = new JSONArray();
            stationResponses.put("Đây là thông tin về ga {ga} mà bạn yêu cầu.");
            stationResponses.put("Ga {ga} có địa chỉ và thông tin liên hệ như sau:");
            stationResponses.put("Tôi đã tìm thấy thông tin về ga {ga} cho bạn.");
            responses.put("station_query", stationResponses);

            // Train query responses
            JSONArray trainResponses = new JSONArray();
            trainResponses.put("Thông tin về tàu {ten_tau}: ");
            trainResponses.put("Tàu {ten_tau} có các chi tiết sau: ");
            trainResponses.put("Dưới đây là thông tin về tàu {ten_tau} mà bạn yêu cầu.");
            responses.put("train_query", trainResponses);

            // Ticket query responses
            JSONArray ticketResponses = new JSONArray();
            ticketResponses.put("Thông tin về vé tàu mà bạn yêu cầu: ");
            ticketResponses.put("Dưới đây là thông tin về giá vé và cách đặt vé: ");
            ticketResponses.put("Tôi đã tìm thấy thông tin vé tàu theo yêu cầu của bạn: ");
            responses.put("ticket_query", ticketResponses);

            // Fallback responses
            JSONArray fallbackResponses = new JSONArray();
            fallbackResponses.put("Xin lỗi, tôi không hiểu ý bạn. Bạn có thể nói rõ hơn được không?");
            fallbackResponses.put("Tôi chưa được đào tạo để hiểu câu hỏi này. Bạn có thể hỏi về lịch trình tàu, trạng thái tàu, hoặc thông tin ga.");
            fallbackResponses.put("Tôi không chắc tôi hiểu ý bạn. Bạn có thể thử hỏi theo cách khác không?");
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