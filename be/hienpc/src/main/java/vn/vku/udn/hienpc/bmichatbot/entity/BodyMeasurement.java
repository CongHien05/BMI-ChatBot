package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "body_measurements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodyMeasurement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Integer measurementId;
    
    @Column(name = "date_recorded")
    private LocalDate dateRecorded;
    
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;
    
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}


