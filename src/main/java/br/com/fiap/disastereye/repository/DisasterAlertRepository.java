package br.com.fiap.disastereye.repository;

import br.com.fiap.disastereye.model.DisasterAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisasterAlertRepository extends JpaRepository<DisasterAlert, Long> {

    Page<DisasterAlert> findByStatus(DisasterAlert.AlertStatus status, Pageable pageable);

    Page<DisasterAlert> findByType(DisasterAlert.DisasterType type, Pageable pageable);

    Page<DisasterAlert> findBySeverity(DisasterAlert.SeverityLevel severity, Pageable pageable);

    List<DisasterAlert> findByStatusOrderByDetectedAtDesc(DisasterAlert.AlertStatus status);

    @Query("""
        SELECT a FROM DisasterAlert a
        WHERE a.status = 'ACTIVE'
        AND (6371 * acos(
            cos(radians(:lat)) * cos(radians(a.latitude)) *
            cos(radians(a.longitude) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(a.latitude))
        )) <= :radiusKm
        ORDER BY a.severity DESC
    """)
    List<DisasterAlert> findActiveAlertsNearLocation(
            @Param("lat") Double latitude,
            @Param("lon") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    @Query("SELECT COUNT(a) FROM DisasterAlert a WHERE a.status = :status")
    long countByStatus(@Param("status") DisasterAlert.AlertStatus status);

    @Query("SELECT a.type, COUNT(a) FROM DisasterAlert a GROUP BY a.type")
    List<Object[]> countByType();
}
