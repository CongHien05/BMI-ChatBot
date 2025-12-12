package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "foods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Integer foodId;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(name = "serving_unit", nullable = false)
    private String servingUnit;

    @Column(name = "calories_per_unit", nullable = false)
    private Integer caloriesPerUnit;
}


