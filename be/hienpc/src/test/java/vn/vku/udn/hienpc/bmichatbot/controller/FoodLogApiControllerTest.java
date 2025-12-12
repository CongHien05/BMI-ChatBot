package vn.vku.udn.hienpc.bmichatbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.vku.udn.hienpc.bmichatbot.controller.api.FoodLogApiController;
import vn.vku.udn.hienpc.bmichatbot.dto.request.FoodLogRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.service.FoodLogService;
import vn.vku.udn.hienpc.bmichatbot.service.JwtService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(FoodLogApiController.class)
class FoodLogApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FoodLogService foodLogService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "test@example.com")
    void getFoods_shouldReturnList() throws Exception {
        Mockito.when(foodLogService.getAllFoods())
                .thenReturn(List.of(new FoodResponse(1, "Apple", "gram", 52)));

        mockMvc.perform(get("/api/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].foodName").value("Apple"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void logFood_shouldReturnOk() throws Exception {
        FoodLogRequest req = new FoodLogRequest(1, new BigDecimal("1.0"), "BREAKFAST");

        mockMvc.perform(post("/api/logs/food")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}


