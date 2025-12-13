package vn.vku.udn.hienpc.bmichatbot.ml;

/**
 * Intent types for chatbot classification
 */
public enum IntentType {
    // Food related
    LOG_FOOD("Log món ăn"),
    GET_FOOD_RECOMMENDATION("Gợi ý món ăn"),
    FOOD_CALORIES_QUERY("Hỏi calories món ăn"),
    
    // Exercise related
    LOG_EXERCISE("Log bài tập"),
    GET_EXERCISE_RECOMMENDATION("Gợi ý bài tập"),
    
    // Weight & Measurement
    VIEW_WEIGHT("Xem cân nặng"),
    UPDATE_WEIGHT("Cập nhật cân nặng"),
    VIEW_BMI("Xem BMI"),
    PREDICT_WEIGHT("Dự đoán cân nặng"),
    
    // Dashboard & Stats
    VIEW_DASHBOARD("Xem tổng quan"),
    VIEW_CALORIES_TODAY("Xem calories hôm nay"),
    VIEW_PROGRESS("Xem tiến độ"),
    
    // Profile & Goals
    VIEW_PROFILE("Xem profile"),
    UPDATE_GOAL("Cập nhật mục tiêu"),
    VIEW_ACHIEVEMENTS("Xem thành tích"),
    
    // General
    GREETING("Chào hỏi"),
    HELP("Hỗ trợ"),
    UNKNOWN("Không xác định");
    
    private final String description;
    
    IntentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

