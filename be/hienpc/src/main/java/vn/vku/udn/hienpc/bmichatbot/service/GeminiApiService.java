package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiApiService {

    @Value("${gemini.api.key:dummy-api-key}")
    private String apiKey;

    public String ask(String query) {
        // TODO: sau này tích hợp thật với Google Gemini API.
        // Tạm thời trả về câu trả lời fallback để không chặn luồng phát triển.
        return "This is a fallback response from Gemini for message: \"" + query + "\"";
    }
}
