package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatbot_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer ruleId;

    @Column(name = "intent", nullable = false)
    private String intent;

    @Column(name = "keywords", columnDefinition = "TEXT", nullable = false)
    private String keywords; // ví dụ: "cân nặng, tăng cân, weight"

    @Column(name = "response_template", columnDefinition = "TEXT", nullable = false)
    private String responseTemplate;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
}


