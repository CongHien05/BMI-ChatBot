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

    // Custom food fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private User createdByUser; // null = created by admin, not null = created by user

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", nullable = true)
    private User createdByAdmin; // null = created by user, not null = created by admin

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true; // true = public (admin created), false = private (user created)
}


