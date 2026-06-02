package br.com.fiap.disastereye.service;

import br.com.fiap.disastereye.dto.request.AlertRequest.*;
import br.com.fiap.disastereye.dto.response.ApiResponse.AlertResponse;
import br.com.fiap.disastereye.dto.response.ApiResponse.DashboardStatsResponse;
import br.com.fiap.disastereye.dto.response.PageResponse;
import br.com.fiap.disastereye.exception.ResourceNotFoundException;
import br.com.fiap.disastereye.model.DisasterAlert;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.repository.DisasterAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisasterAlertService {

    private final DisasterAlertRepository alertRepository;

    // ─── Listagem com filtros e paginação ────────────────────────────────────

    public PageResponse<AlertResponse> listAlerts(
            DisasterAlert.AlertStatus status,
            DisasterAlert.DisasterType type,
            DisasterAlert.SeverityLevel severity,
            int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "detectedAt"));

        var resultPage = (status != null)   ? alertRepository.findByStatus(status, pageable)
                       : (type != null)     ? alertRepository.findByType(type, pageable)
                       : (severity != null) ? alertRepository.findBySeverity(severity, pageable)
                       : alertRepository.findAll(pageable);

        return PageResponse.from(resultPage.map(AlertResponse::from));
    }

    // ─── Busca por ID ────────────────────────────────────────────────────────

    public AlertResponse findById(Long id) {
        return alertRepository.findById(id)
                .map(AlertResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));
    }

    // ─── Alertas próximos (geoespacial) ──────────────────────────────────────

    public List<AlertResponse> findNearby(Double lat, Double lon, Double radiusKm) {
        double radius = (radiusKm != null && radiusKm > 0) ? radiusKm : 100.0;
        log.debug("Buscando alertas em {}, {} num raio de {}km", lat, lon, radius);
        return alertRepository.findActiveAlertsNearLocation(lat, lon, radius)
                .stream()
                .map(AlertResponse::from)
                .collect(Collectors.toList());
    }

    // ─── Criar alerta ─────────────────────────────────────────────────────────

    @Transactional
    public AlertResponse create(CreateAlertRequest request, User currentUser) {
        DisasterAlert alert = DisasterAlert.builder()
                .title(request.title())
                .description(request.description())
                .type(request.type())
                .severity(request.severity())
                .status(DisasterAlert.AlertStatus.ACTIVE)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .location(request.location())
                .affectedRadiusKm(request.affectedRadiusKm())
                .nasaEventId(request.nasaEventId())
                .satelliteSource(request.satelliteSource())
                .detectedAt(request.detectedAt())
                .createdBy(currentUser)
                .build();

        DisasterAlert saved = alertRepository.save(alert);
        log.info("Novo alerta criado [id={}] tipo={} severidade={} por={}",
                saved.getId(), saved.getType(), saved.getSeverity(), currentUser.getEmail());
        return AlertResponse.from(saved);
    }

    // ─── Atualizar alerta ────────────────────────────────────────────────────

    @Transactional
    public AlertResponse update(Long id, UpdateAlertRequest request) {
        DisasterAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));

        if (request.title() != null) alert.setTitle(request.title());
        if (request.description() != null) alert.setDescription(request.description());
        if (request.severity() != null) alert.setSeverity(request.severity());
        if (request.status() != null) alert.setStatus(request.status());
        if (request.affectedRadiusKm() != null) alert.setAffectedRadiusKm(request.affectedRadiusKm());

        log.info("Alerta atualizado [id={}]", id);
        return AlertResponse.from(alertRepository.save(alert));
    }

    // ─── Deletar alerta ──────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alerta", id);
        }
        alertRepository.deleteById(id);
        log.info("Alerta deletado [id={}]", id);
    }

    // ─── Dashboard / Estatísticas ────────────────────────────────────────────

    public DashboardStatsResponse getDashboardStats() {
        long total    = alertRepository.count();
        long active   = alertRepository.countByStatus(DisasterAlert.AlertStatus.ACTIVE);
        long resolved = alertRepository.countByStatus(DisasterAlert.AlertStatus.RESOLVED);
        long critical = alertRepository.findBySeverity(DisasterAlert.SeverityLevel.CRITICAL,
                PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();

        Map<String, Long> byType = alertRepository.countByType()
                .stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));

        return new DashboardStatsResponse(total, active, resolved, critical, byType);
    }
}
