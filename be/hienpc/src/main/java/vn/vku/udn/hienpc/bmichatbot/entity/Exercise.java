package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private Integer exerciseId;

    @Column(name = "exercise_name", nullable = false)
    private String exerciseName;

    @Column(name = "calories_burned_per_hour", nullable = false)
    private Integer caloriesBurnedPerHour;

    // Custom exercise fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private User createdByUser; // null = created by admin, not null = created by user

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", nullable = true)
    private User createdByAdmin; // null = created by user, not null = created by admin

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true; // true = public (admin created), false = private (user created)
}


