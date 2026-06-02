package br.com.fiap.disastereye.repository;

import br.com.fiap.disastereye.model.AlertReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertReportRepository extends JpaRepository<AlertReport, Long> {
    Page<AlertReport> findByAlertId(Long alertId, Pageable pageable);
    Page<AlertReport> findByReportedById(Long userId, Pageable pageable);
}
