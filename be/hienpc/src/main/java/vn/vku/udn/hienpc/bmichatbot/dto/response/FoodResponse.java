package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponse {
    private Integer foodId;
    private String foodName;
    private String servingUnit;
    private Integer caloriesPerUnit;
}
