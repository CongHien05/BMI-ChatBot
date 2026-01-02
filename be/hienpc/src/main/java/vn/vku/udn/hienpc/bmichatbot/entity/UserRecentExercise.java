package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_recent_exercises", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exercise_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRecentExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recent_id")
    private Integer recentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUsedAt = LocalDateTime.now();
    }
}

