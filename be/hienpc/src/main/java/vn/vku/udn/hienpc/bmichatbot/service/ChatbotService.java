package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatbotRule;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatbotRuleRepository;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentClassifierService;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentClassifierService.IntentResult;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentType;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ChatbotService {

    private final ChatbotRuleRepository chatbotRuleRepository;
    private final GeminiApiService geminiApiService;
    private final IntentClassifierService intentClassifier;

    public ChatbotService(ChatbotRuleRepository chatbotRuleRepository,
                          GeminiApiService geminiApiService,
                          IntentClassifierService intentClassifier) {
        this.chatbotRuleRepository = chatbotRuleRepository;
        this.geminiApiService = geminiApiService;
        this.intentClassifier = intentClassifier;
    }

    public ChatResponse handleMessage(String userMessage, String traceId) {
        long startTime = System.currentTimeMillis();
        
        if (!StringUtils.hasText(userMessage)) {
            return ChatResponse.builder()
                    .reply("Xin lỗi, mình không nhận được nội dung tin nhắn của bạn.")
                    .source("RULE")
                    .traceId(traceId)
                    .durationMs(0L)
                    .build();
        }

        String normalized = userMessage.toLowerCase(Locale.ROOT);

        // Bước 1: AI Intent Classification (Naive Bayes)
        IntentResult intentResult = intentClassifier.classifyWithConfidence(userMessage);
        IntentType intent = intentResult.getIntent();
        double confidence = intentResult.getConfidence();
        
        // If confidence is high enough (> 60%), use intent-based response
        if (confidence > 0.6 && intent != IntentType.UNKNOWN) {
            String reply = getIntentBasedResponse(intent, userMessage);
            long duration = System.currentTimeMillis() - startTime;
            
            return ChatResponse.builder()
                    .reply(reply + String.format("\n\n🤖 AI detected: %s (%.0f%% confidence)", 
                        intent.getDescription(), confidence * 100))
                    .source("AI_INTENT")
                    .traceId(traceId)
                    .durationMs(duration)
                    .build();
        }

        // Bước 2: Fallback to keyword-based rules
        List<ChatbotRule> allRules = new java.util.ArrayList<>(chatbotRuleRepository.findAll());
        allRules.sort(Comparator.comparing(ChatbotRule::getPriority).reversed());

        for (ChatbotRule rule : allRules) {
            if (matchRuleByKeywords(rule, normalized)) {
                long duration = System.currentTimeMillis() - startTime;
                return ChatResponse.builder()
                        .reply(rule.getResponseTemplate())
                        .source("RULE")
                        .traceId(traceId)
                        .durationMs(duration)
                        .build();
            }
        }

        // Bước 3: Fallback to Gemini
        String fallback = geminiApiService.ask(userMessage);
        long duration = System.currentTimeMillis() - startTime;
        
        return ChatResponse.builder()
                .reply(fallback)
                .source("GEMINI")
                .traceId(traceId)
                .durationMs(duration)
                .build();
    }
    
    /**
     * Generate response based on detected intent
     */
    private String getIntentBasedResponse(IntentType intent, String userMessage) {
        switch (intent) {
            case LOG_FOOD:
                return "Bạn muốn log món ăn đúng không? Vui lòng vào tab \"Log\" → \"Food\" để ghi lại bữa ăn của bạn. " +
                       "Bạn có thể chọn món từ danh sách hoặc xem gợi ý AI phía dưới!";
                       
            case GET_FOOD_RECOMMENDATION:
                return "Tôi có thể gợi ý món ăn phù hợp cho bạn! Vào tab \"Log\" → \"Food\" và scroll xuống, " +
                       "bạn sẽ thấy phần \"🤖 AI Gợi ý món ăn\" với các món được cá nhân hóa dựa trên profile của bạn.";
                       
            case LOG_EXERCISE:
                return "Tuyệt vời! Bạn muốn log bài tập. Vui lòng vào tab \"Log\" → \"Exercise\" để ghi lại " +
                       "bài tập và thời gian tập luyện của bạn.";
                       
            case GET_EXERCISE_RECOMMENDATION:
                return "Tôi có thể gợi ý bài tập phù hợp! Vào tab \"Log\" → \"Exercise\" và scroll xuống, " +
                       "bạn sẽ thấy \"🤖 AI Gợi ý bài tập\" với các bài tập được AI đề xuất cho bạn.";
                       
            case VIEW_WEIGHT:
                return "Để xem cân nặng hiện tại, vui lòng vào tab \"Dashboard\" (trang chủ). " +
                       "Bạn sẽ thấy cân nặng mới nhất ở card đầu tiên.";
                       
            case UPDATE_WEIGHT:
                return "Để cập nhật cân nặng, vào tab \"Dashboard\" và nhấn nút \"Cập nhật chỉ số\". " +
                       "Nhập cân nặng và chiều cao (nếu thay đổi) của bạn.";
                       
            case VIEW_BMI:
                return "Để xem chỉ số BMI, vui lòng vào tab \"Dashboard\". " +
                       "BMI của bạn được hiển thị ở card thứ hai với phân loại (Thiếu cân / Bình thường / Thừa cân).";
                       
            case PREDICT_WEIGHT:
                return "AI có thể dự đoán cân nặng tương lai của bạn! Vào tab \"Dashboard\" và scroll xuống, " +
                       "bạn sẽ thấy \"🤖 AI Dự đoán cân nặng (7 ngày)\" với biểu đồ dự báo xu hướng cân nặng.";
                       
            case VIEW_DASHBOARD:
                return "Tab \"Dashboard\" (trang chủ) hiển thị tổng quan về: " +
                       "\n• Cân nặng hiện tại\n• Chỉ số BMI\n• Calories hôm nay" +
                       "\n• Biểu đồ xu hướng\n• Dự đoán cân nặng AI\n• Thống kê 7 ngày qua";
                       
            case VIEW_CALORIES_TODAY:
                return "Để xem calories hôm nay, vào tab \"Dashboard\". " +
                       "Calories bạn đã nạp được hiển thị ở card thứ ba với progress bar.";
                       
            case VIEW_ACHIEVEMENTS:
                return "Để xem thành tích và huy chương, vào tab \"Profile\" (biểu tượng người dùng). " +
                       "Bạn sẽ thấy tất cả achievements đã đạt được và streak hiện tại.";
                       
            case GREETING:
                return "Xin chào! 👋 Tôi là BMI Chatbot, trợ lý AI của bạn. " +
                       "Tôi có thể giúp bạn:\n" +
                       "• Log món ăn & bài tập\n" +
                       "• Xem cân nặng & BMI\n" +
                       "• Dự đoán cân nặng tương lai\n" +
                       "• Gợi ý món ăn & bài tập phù hợp\n\n" +
                       "Hãy hỏi tôi bất cứ điều gì!";
                       
            case HELP:
                return "Tôi có thể giúp bạn với:\n\n" +
                       "📊 **Dashboard**: Xem tổng quan, BMI, calories, dự đoán AI\n" +
                       "🍽️ **Log Food**: Ghi món ăn, xem gợi ý AI\n" +
                       "💪 **Log Exercise**: Ghi bài tập, xem gợi ý AI\n" +
                       "👤 **Profile**: Xem achievements, streak, cập nhật thông tin\n\n" +
                       "Hãy thử hỏi: \"Gợi ý món ăn cho tôi\" hoặc \"Dự đoán cân nặng\"";
                       
            default:
                return "Tôi hiểu bạn đang hỏi về " + intent.getDescription() + ". " +
                       "Vui lòng thử câu hỏi cụ thể hơn hoặc gõ \"help\" để xem hướng dẫn.";
        }
    }

    private boolean matchRuleByKeywords(ChatbotRule rule, String normalizedMessage) {
        if (!StringUtils.hasText(rule.getKeywords())) {
            return false;
        }
        String[] tokens = rule.getKeywords().split(",");
        for (String rawKeyword : tokens) {
            String keyword = rawKeyword.trim().toLowerCase(Locale.ROOT);
            if (!keyword.isEmpty() && normalizedMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
