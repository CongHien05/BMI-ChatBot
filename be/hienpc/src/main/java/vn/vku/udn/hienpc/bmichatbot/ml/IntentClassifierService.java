package vn.vku.udn.hienpc.bmichatbot.ml;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Naive Bayes Intent Classification Service
 * Phân loại ý định người dùng từ tin nhắn chat
 */
@Service
public class IntentClassifierService {
    
    // Training data: Intent -> List of example phrases
    private Map<IntentType, List<String>> trainingData = new HashMap<>();
    
    // Vocabulary: All unique words from training data
    private Set<String> vocabulary = new HashSet<>();
    
    // Prior probabilities: P(Intent)
    private Map<IntentType, Double> priorProbabilities = new HashMap<>();
    
    // Word frequencies: P(word | Intent)
    private Map<IntentType, Map<String, Integer>> wordFrequencies = new HashMap<>();
    
    // Total words per intent
    private Map<IntentType, Integer> totalWordsPerIntent = new HashMap<>();
    
    // Vietnamese stopwords (từ dừng)
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "tôi", "của", "là", "và", "có", "được", "cho", "từ", "này", "đó",
        "một", "với", "các", "người", "trong", "để", "không", "họ", "như",
        "về", "rằng", "vào", "mà", "ở", "đã", "sẽ", "hay", "hoặc", "nhưng"
    ));
    
    @PostConstruct
    public void initialize() {
        loadTrainingData();
        train();
    }
    
    /**
     * Load training examples for each intent
     */
    private void loadTrainingData() {
        // LOG_FOOD intent
        trainingData.put(IntentType.LOG_FOOD, Arrays.asList(
            "tôi muốn log bữa sáng",
            "ghi lại bữa ăn",
            "log món ăn",
            "tôi vừa ăn phở",
            "ăn cơm tấm",
            "thêm món ăn",
            "ghi bữa trưa",
            "log bữa tối",
            "tôi ăn bánh mì sáng nay",
            "muốn ghi lại món ăn"
        ));
        
        // GET_FOOD_RECOMMENDATION intent
        trainingData.put(IntentType.GET_FOOD_RECOMMENDATION, Arrays.asList(
            "gợi ý món ăn cho tôi",
            "tôi nên ăn gì",
            "món nào tốt cho tôi",
            "đề xuất món ăn",
            "nên ăn món gì hôm nay",
            "gợi ý thực đơn",
            "món ăn phù hợp",
            "cho tôi gợi ý món",
            "tư vấn món ăn",
            "nên ăn gì để giảm cân"
        ));
        
        // LOG_EXERCISE intent
        trainingData.put(IntentType.LOG_EXERCISE, Arrays.asList(
            "tôi vừa chạy bộ",
            "log bài tập",
            "ghi lại tập luyện",
            "tôi tập gym",
            "vừa tập thể dục",
            "chạy 30 phút",
            "ghi bài tập",
            "log tập luyện",
            "tôi vừa bơi",
            "đi bộ 1 tiếng"
        ));
        
        // GET_EXERCISE_RECOMMENDATION intent
        trainingData.put(IntentType.GET_EXERCISE_RECOMMENDATION, Arrays.asList(
            "gợi ý bài tập cho tôi",
            "tôi nên tập gì",
            "bài tập nào tốt",
            "đề xuất bài tập",
            "nên tập gì hôm nay",
            "gợi ý luyện tập",
            "bài tập phù hợp",
            "tư vấn tập luyện",
            "nên tập gì để giảm cân",
            "bài tập giảm mỡ bụng"
        ));
        
        // VIEW_WEIGHT intent
        trainingData.put(IntentType.VIEW_WEIGHT, Arrays.asList(
            "cân nặng của tôi",
            "tôi nặng bao nhiêu",
            "xem cân nặng",
            "hiện tại tôi nặng bao nhiêu",
            "kiểm tra cân nặng",
            "cân nặng hiện tại",
            "cho tôi biết cân nặng",
            "tôi bao nhiêu kg",
            "xem trọng lượng",
            "cân nặng bây giờ"
        ));
        
        // UPDATE_WEIGHT intent
        trainingData.put(IntentType.UPDATE_WEIGHT, Arrays.asList(
            "cập nhật cân nặng",
            "tôi muốn nhập cân nặng",
            "ghi lại cân nặng mới",
            "cân nặng của tôi là 70kg",
            "update cân nặng",
            "thay đổi cân nặng",
            "sửa cân nặng",
            "nhập số cân",
            "tôi cân được 65kg",
            "cân mới là 68kg"
        ));
        
        // VIEW_BMI intent
        trainingData.put(IntentType.VIEW_BMI, Arrays.asList(
            "BMI của tôi",
            "xem BMI",
            "chỉ số BMI",
            "tính BMI",
            "kiểm tra BMI",
            "BMI hiện tại",
            "cho biết BMI của tôi",
            "tôi có BMI bao nhiêu",
            "xem chỉ số cơ thể",
            "tính chỉ số khối cơ thể"
        ));
        
        // PREDICT_WEIGHT intent
        trainingData.put(IntentType.PREDICT_WEIGHT, Arrays.asList(
            "dự đoán cân nặng",
            "tôi sẽ nặng bao nhiêu",
            "cân nặng tương lai",
            "predict cân nặng",
            "tôi sẽ nặng bao nhiêu tuần sau",
            "dự báo cân nặng",
            "cân nặng sau 1 tháng",
            "tính toán cân nặng tương lai",
            "xu hướng cân nặng",
            "cân nặng của tôi sẽ thế nào"
        ));
        
        // VIEW_DASHBOARD intent
        trainingData.put(IntentType.VIEW_DASHBOARD, Arrays.asList(
            "xem tổng quan",
            "dashboard",
            "bảng điều khiển",
            "xem số liệu tổng hợp",
            "hiển thị tổng quan",
            "thống kê chung",
            "xem overview",
            "tình hình chung",
            "báo cáo tổng quan",
            "số liệu hôm nay"
        ));
        
        // VIEW_CALORIES_TODAY intent
        trainingData.put(IntentType.VIEW_CALORIES_TODAY, Arrays.asList(
            "calories hôm nay",
            "tôi ăn bao nhiêu calories",
            "xem calories",
            "kiểm tra calories",
            "tổng calories",
            "calories đã nạp",
            "tôi tiêu thụ bao nhiêu calories",
            "calories intake",
            "năng lượng hôm nay",
            "đã ăn bao nhiêu calo"
        ));
        
        // VIEW_ACHIEVEMENTS intent
        trainingData.put(IntentType.VIEW_ACHIEVEMENTS, Arrays.asList(
            "xem thành tích",
            "achievements",
            "huy chương của tôi",
            "thành tựu",
            "danh hiệu",
            "xem badges",
            "tôi có bao nhiêu huy chương",
            "thành tích đạt được",
            "phần thưởng",
            "xem giải thưởng"
        ));
        
        // GREETING intent
        trainingData.put(IntentType.GREETING, Arrays.asList(
            "xin chào",
            "chào bạn",
            "hello",
            "hi",
            "hey",
            "chào",
            "good morning",
            "buổi sáng tốt lành",
            "chào buổi tối",
            "hế nhô"
        ));
        
        // HELP intent
        trainingData.put(IntentType.HELP, Arrays.asList(
            "giúp đỡ",
            "help",
            "hướng dẫn",
            "tôi cần trợ giúp",
            "làm thế nào",
            "hỗ trợ",
            "không biết dùng",
            "chỉ cho tôi",
            "tôi không hiểu",
            "hướng dẫn sử dụng"
        ));
    }
    
    /**
     * Train Naive Bayes classifier
     */
    private void train() {
        int totalExamples = 0;
        
        // Count total examples
        for (List<String> examples : trainingData.values()) {
            totalExamples += examples.size();
        }
        
        // Calculate prior probabilities and word frequencies
        for (Map.Entry<IntentType, List<String>> entry : trainingData.entrySet()) {
            IntentType intent = entry.getKey();
            List<String> examples = entry.getValue();
            
            // Prior probability: P(Intent) = count(Intent) / totalExamples
            priorProbabilities.put(intent, (double) examples.size() / totalExamples);
            
            // Initialize word frequency map for this intent
            Map<String, Integer> wordFreq = new HashMap<>();
            int totalWords = 0;
            
            // Count word frequencies
            for (String example : examples) {
                List<String> words = preprocessText(example);
                for (String word : words) {
                    vocabulary.add(word);
                    wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                    totalWords++;
                }
            }
            
            wordFrequencies.put(intent, wordFreq);
            totalWordsPerIntent.put(intent, totalWords);
        }
    }
    
    /**
     * Classify user message and return intent
     */
    public IntentType classify(String message) {
        List<String> words = preprocessText(message);
        
        if (words.isEmpty()) {
            return IntentType.UNKNOWN;
        }
        
        double maxProbability = Double.NEGATIVE_INFINITY;
        IntentType bestIntent = IntentType.UNKNOWN;
        
        // Calculate probability for each intent
        for (IntentType intent : trainingData.keySet()) {
            double probability = Math.log(priorProbabilities.get(intent));
            
            Map<String, Integer> wordFreq = wordFrequencies.get(intent);
            int totalWords = totalWordsPerIntent.get(intent);
            int vocabularySize = vocabulary.size();
            
            // Calculate P(message | intent) using Laplace smoothing
            for (String word : words) {
                int wordCount = wordFreq.getOrDefault(word, 0);
                // Laplace smoothing: (count + 1) / (total + vocabulary_size)
                double wordProbability = (double) (wordCount + 1) / (totalWords + vocabularySize);
                probability += Math.log(wordProbability);
            }
            
            if (probability > maxProbability) {
                maxProbability = probability;
                bestIntent = intent;
            }
        }
        
        return bestIntent;
    }
    
    /**
     * Classify and return with confidence score
     */
    public IntentResult classifyWithConfidence(String message) {
        List<String> words = preprocessText(message);
        
        if (words.isEmpty()) {
            return new IntentResult(IntentType.UNKNOWN, 0.0);
        }
        
        Map<IntentType, Double> probabilities = new HashMap<>();
        
        // Calculate probability for each intent
        for (IntentType intent : trainingData.keySet()) {
            double probability = Math.log(priorProbabilities.get(intent));
            
            Map<String, Integer> wordFreq = wordFrequencies.get(intent);
            int totalWords = totalWordsPerIntent.get(intent);
            int vocabularySize = vocabulary.size();
            
            for (String word : words) {
                int wordCount = wordFreq.getOrDefault(word, 0);
                double wordProbability = (double) (wordCount + 1) / (totalWords + vocabularySize);
                probability += Math.log(wordProbability);
            }
            
            probabilities.put(intent, probability);
        }
        
        // Find best intent and calculate confidence
        IntentType bestIntent = IntentType.UNKNOWN;
        double maxProb = Double.NEGATIVE_INFINITY;
        
        for (Map.Entry<IntentType, Double> entry : probabilities.entrySet()) {
            if (entry.getValue() > maxProb) {
                maxProb = entry.getValue();
                bestIntent = entry.getKey();
            }
        }
        
        // Convert log probability to confidence (0-1)
        // Normalize using softmax
        double sumExp = probabilities.values().stream()
            .mapToDouble(Math::exp)
            .sum();
        double confidence = Math.exp(maxProb) / sumExp;
        
        return new IntentResult(bestIntent, confidence);
    }
    
    /**
     * Preprocess text: lowercase, tokenize, remove stopwords
     */
    private List<String> preprocessText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Lowercase
        text = text.toLowerCase().trim();
        
        // Remove special characters, keep only letters and spaces
        text = text.replaceAll("[^a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ\\s]", "");
        
        // Tokenize by spaces
        String[] tokens = text.split("\\s+");
        
        // Remove stopwords
        return Arrays.stream(tokens)
            .filter(token -> !token.isEmpty() && !STOPWORDS.contains(token))
            .collect(Collectors.toList());
    }
    
    /**
     * Result class with intent and confidence
     */
    public static class IntentResult {
        private final IntentType intent;
        private final double confidence;
        
        public IntentResult(IntentType intent, double confidence) {
            this.intent = intent;
            this.confidence = confidence;
        }
        
        public IntentType getIntent() {
            return intent;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        @Override
        public String toString() {
            return String.format("Intent: %s (Confidence: %.2f%%)", 
                intent.getDescription(), confidence * 100);
        }
    }
}

