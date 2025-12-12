package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Achievement - Badges/Thành tựu của user
 */
@Entity
@Table(name = "user_achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "achievement_id")
    private Integer achievementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "achievement_type", nullable = false)
    private String achievementType; // "FIRST_LOG", "7_DAY_STREAK", "30_DAY_STREAK", "GOAL_ACHIEVED", etc.

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata (optional details)
}

