package br.com.fiap.disastereye.service;

import br.com.fiap.disastereye.dto.request.ReportRequest.CreateReportRequest;
import br.com.fiap.disastereye.dto.response.PageResponse;
import br.com.fiap.disastereye.exception.ResourceNotFoundException;
import br.com.fiap.disastereye.model.AlertReport;
import br.com.fiap.disastereye.model.DisasterAlert;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.repository.AlertReportRepository;
import br.com.fiap.disastereye.repository.DisasterAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertReportService {

    private final AlertReportRepository reportRepository;
    private final DisasterAlertRepository alertRepository;

    public record ReportResponse(
            Long id,
            Long alertId,
            String alertTitle,
            String reportedBy,
            String description,
            AlertReport.ReportStatus status,
            Integer peopleAffected,
            Boolean infrastructureDamage,
            LocalDateTime createdAt
    ) {
        public static ReportResponse from(AlertReport r) {
            return new ReportResponse(
                    r.getId(),
                    r.getAlert().getId(),
                    r.getAlert().getTitle(),
                    r.getReportedBy().getName(),
                    r.getDescription(),
                    r.getStatus(),
                    r.getPeopleAffected(),
                    r.getInfrastructureDamage(),
                    r.getCreatedAt()
            );
        }
    }

    @Transactional
    public ReportResponse create(CreateReportRequest request, User user) {
        DisasterAlert alert = alertRepository.findById(request.alertId())
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", request.alertId()));

        AlertReport report = AlertReport.builder()
                .alert(alert)
                .reportedBy(user)
                .description(request.description())
                .peopleAffected(request.peopleAffected())
                .infrastructureDamage(request.infrastructureDamage())
                .status(AlertReport.ReportStatus.PENDING)
                .build();

        AlertReport saved = reportRepository.save(report);
        log.info("Relatório criado [id={}] para alerta [id={}] por {}", saved.getId(), alert.getId(), user.getEmail());
        return ReportResponse.from(saved);
    }

    public PageResponse<ReportResponse> listByAlert(Long alertId, int page, int size) {
        if (!alertRepository.existsById(alertId)) {
            throw new ResourceNotFoundException("Alerta", alertId);
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(reportRepository.findByAlertId(alertId, pageable).map(ReportResponse::from));
    }

    public ReportResponse findById(Long id) {
        return reportRepository.findById(id)
                .map(ReportResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Relatório", id));
    }

    @Transactional
    public ReportResponse updateStatus(Long id, AlertReport.ReportStatus status) {
        AlertReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatório", id));
        report.setStatus(status);
        log.info("Status do relatório [id={}] atualizado para {}", id, status);
        return ReportResponse.from(reportRepository.save(report));
    }
}
