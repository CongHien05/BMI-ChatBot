package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * User Streak - Theo dõi streak logging của user
 */
@Entity
@Table(name = "user_streaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "streak_id")
    private Integer streakId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0; // Số ngày liên tiếp log

    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0; // Streak dài nhất từng đạt được

    @Column(name = "last_log_date")
    private LocalDate lastLogDate; // Ngày log gần nhất

    @Column(name = "updated_at")
    private LocalDate updatedAt;
}

