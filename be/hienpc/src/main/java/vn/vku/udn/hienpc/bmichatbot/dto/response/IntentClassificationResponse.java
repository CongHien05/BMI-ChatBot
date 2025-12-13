package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentClassificationResponse {
    private String message;
    private String intent;
    private String intentDescription;
    private double confidence;
    private String suggestedResponse;
}

