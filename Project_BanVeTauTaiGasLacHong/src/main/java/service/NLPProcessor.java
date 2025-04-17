package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Lớp này xử lý việc hiểu ý định từ câu hỏi của người dùng
 * Phiên bản đơn giản sử dụng từ khóa và biểu thức chính quy
 */
public class NLPProcessor {
    private static final Logger LOGGER = Logger.getLogger(NLPProcessor.class.getName());

    // Các mẫu câu và từ khóa theo ý định
    private final Map<String, List<String>> intentPatterns;

    // Biểu thức chính quy để trích xuất thông tin
    private final Map<String, Pattern> extractionPatterns;

    // Danh sách các từ phủ định
    private final List<String> negationWords;

    public NLPProcessor() {
        // Khởi tạo
        intentPatterns = new HashMap<>();
        extractionPatterns = new HashMap<>();
        negationWords = Arrays.asList("không", "chưa", "đừng", "đã không", "không thể");

        initializeIntentPatterns();
        initializeExtractionPatterns();
    }

    private void initializeIntentPatterns() {
        // Mẫu cho việc tìm lịch trình
        intentPatterns.put("SEARCH_SCHEDULE", Arrays.asList(
                "lịch trình", "chuyến tàu", "giờ tàu", "tàu chạy", "tìm tàu",
                "có tàu nào", "khi nào có tàu", "giờ khởi hành", "tàu đi lúc mấy giờ",
                "từ .* đến .*", "đi .* về .*"
        ));

        // Mẫu cho kiểm tra trạng thái
        intentPatterns.put("CHECK_STATUS", Arrays.asList(
                "trạng thái", "tình trạng", "đã chạy chưa", "đã khởi hành chưa",
                "có đúng giờ không", "bị trễ", "đã đến chưa", "tàu đang ở đâu",
                "còn chạy không", "hoạt động"
        ));

        // Mẫu cho đặt vé
        intentPatterns.put("BOOK_TICKET", Arrays.asList(
                "đặt vé", "mua vé", "giữ chỗ", "đặt chỗ", "còn vé", "hết vé chưa",
                "giá vé", "vé tàu", "thanh toán", "đặt online", "đặt trước"
        ));

        // Mẫu cho thông tin ga tàu
        intentPatterns.put("STATION_INFO", Arrays.asList(
                "ga tàu", "nhà ga", "địa chỉ ga", "ga .* ở đâu", "đến ga", "điểm đón",
                "điểm trả", "vị trí ga"
        ));

        // Mẫu cho ước tính thời gian
        intentPatterns.put("ESTIMATE_ARRIVAL", Arrays.asList(
                "mất bao lâu", "đến lúc mấy giờ", "mấy giờ đến", "thời gian di chuyển",
                "bao lâu thì đến", "khi nào đến", "đến kịp không", "bao lâu để đi từ",
                "mất bao nhiêu thời gian"
        ));

        // Mẫu cho câu chào và cảm ơn
        intentPatterns.put("GREETING", Arrays.asList(
                "xin chào", "chào bạn", "hello", "hi", "hey", "chào buổi",
                "tạm biệt", "goodbye", "bye"
        ));

        intentPatterns.put("THANKS", Arrays.asList(
                "cảm ơn", "thanks", "thank you", "cám ơn", "cảm tạ"
        ));

        // Mẫu cho câu lỗi và phàn nàn
        intentPatterns.put("COMPLAINT", Arrays.asList(
                "phàn nàn", "khiếu nại", "không hài lòng", "tệ quá", "kém", "chậm trễ",
                "thất vọng", "tồi tệ", "dở", "kinh khủng", "thua kém", "giá cao"
        ));
    }

    private void initializeExtractionPatterns() {
        // Mẫu để trích xuất ga đi và ga đến
        extractionPatterns.put("ROUTE", Pattern.compile(
                "(?:từ|đi|tại)\\s+([\\p{L}\\s]+)(?:đến|về|tới)\\s+([\\p{L}\\s]+)",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
        ));

        // Mẫu để trích xuất ngày đi
        extractionPatterns.put("DATE", Pattern.compile(
                "(?:ngày|vào|đi)\\s+(\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?)|(?:(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?)",
                Pattern.CASE_INSENSITIVE
        ));

        // Mẫu để trích xuất giờ đi
        extractionPatterns.put("TIME", Pattern.compile(
                "(?:lúc|giờ|vào)\\s+(\\d{1,2})(?::|giờ|h)\\s*(\\d{0,2})\\s*(?:phút|p)?",
                Pattern.CASE_INSENSITIVE
        ));

        // Mẫu để trích xuất mã lịch trình
        extractionPatterns.put("SCHEDULE_ID", Pattern.compile(
                "(?:mã|lịch trình|chuyến|số|id)\\s+([A-Za-z0-9]+)",
                Pattern.CASE_INSENSITIVE
        ));
    }

    /**
     * Phát hiện ý định từ tin nhắn của người dùng
     * @param userMessage Tin nhắn của người dùng
     * @return Chuỗi mã ý định
     */
    public String detectIntent(String userMessage) {
        // Chuẩn hóa tin nhắn: chuyển về chữ thường và loại bỏ dấu câu
        String normalizedMessage = userMessage.toLowerCase()
                .replaceAll("[.,!?;:]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Kiểm tra từng ý định
        Map<String, Integer> intentScores = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : intentPatterns.entrySet()) {
            String intent = entry.getKey();
            List<String> patterns = entry.getValue();

            int score = 0;
            for (String pattern : patterns) {
                if (normalizedMessage.contains(pattern) ||
                        normalizedMessage.matches(".*" + pattern + ".*")) {
                    score++;
                }
            }

            if (score > 0) {
                intentScores.put(intent, score);
            }
        }

        // Nếu không phát hiện ý định nào
        if (intentScores.isEmpty()) {
            return "UNKNOWN";
        }

        // Trả về ý định có điểm cao nhất
        return intentScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }

    /**
     * Trích xuất thông tin từ tin nhắn của người dùng
     * @param userMessage Tin nhắn của người dùng
     * @param patternKey Khóa mẫu trích xuất
     * @return Danh sách kết quả trích xuất
     */
    public List<String> extractInformation(String userMessage, String patternKey) {
        if (!extractionPatterns.containsKey(patternKey)) {
            return new ArrayList<>();
        }

        Pattern pattern = extractionPatterns.get(patternKey);
        Matcher matcher = pattern.matcher(userMessage);

        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            // Thêm tất cả các nhóm bắt được
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
                    results.add(matcher.group(i).trim());
                }
            }
        }

        return results;
    }

    /**
     * Phát hiện câu phủ định trong tin nhắn
     * @param userMessage Tin nhắn của người dùng
     * @return true nếu là câu phủ định, false nếu không
     */
    public boolean isNegation(String userMessage) {
        String normalizedMessage = userMessage.toLowerCase();

        for (String word : negationWords) {
            if (normalizedMessage.contains(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Nhận dạng từ khóa trong tin nhắn
     * @param userMessage Tin nhắn của người dùng
     * @param keywords Danh sách từ khóa cần kiểm tra
     * @return Danh sách từ khóa tìm thấy
     */
    public List<String> identifyKeywords(String userMessage, List<String> keywords) {
        String normalizedMessage = userMessage.toLowerCase();
        List<String> foundKeywords = new ArrayList<>();

        for (String keyword : keywords) {
            if (normalizedMessage.contains(keyword.toLowerCase())) {
                foundKeywords.add(keyword);
            }
        }

        return foundKeywords;
    }
}