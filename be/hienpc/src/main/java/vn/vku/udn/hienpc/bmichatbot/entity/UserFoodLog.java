package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_food_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "date_eaten", nullable = false)
    private LocalDateTime dateEaten;

    @Column(name = "meal_type", nullable = false)
    private String mealType;

    @Column(name = "quantity", nullable = false, precision = 6, scale = 2)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;
}


