package vn.vku.udn.hienpc.bmichatbot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bmiChatbotOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("BMI Chatbot API")
                        .description("REST API specification for BMI Chatbot backend")
                        .version("1.0.0"));
    }
}


