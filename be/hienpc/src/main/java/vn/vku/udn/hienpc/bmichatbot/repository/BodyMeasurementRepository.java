package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.BodyMeasurement;

import java.util.List;

@Repository
public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Integer> {
    List<BodyMeasurement> findByUserUserId(Integer userId);
    List<BodyMeasurement> findByUserUserIdOrderByDateRecordedDesc(Integer userId);
}


