package br.com.fiap.disastereye.controller;

import br.com.fiap.disastereye.dto.request.ReportRequest.CreateReportRequest;
import br.com.fiap.disastereye.dto.response.ApiResponse.SuccessResponse;
import br.com.fiap.disastereye.dto.response.PageResponse;
import br.com.fiap.disastereye.model.AlertReport;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.service.AlertReportService;
import br.com.fiap.disastereye.service.AlertReportService.ReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios")
@SecurityRequirement(name = "BearerAuth")
public class AlertReportController {

    private final AlertReportService reportService;

    @PostMapping
    @Operation(summary = "Enviar relatório de campo",
               description = "Usuário autenticado envia um relato sobre um alerta ativo em sua região")
    public ResponseEntity<SuccessResponse<ReportResponse>> create(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ReportResponse report = reportService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Relatório enviado com sucesso", report));
    }

    @GetMapping("/alert/{alertId}")
    @Operation(summary = "Listar relatórios de um alerta",
               description = "Retorna todos os relatórios de campo associados a um alerta específico")
    public ResponseEntity<SuccessResponse<PageResponse<ReportResponse>>> listByAlert(
            @PathVariable Long alertId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(SuccessResponse.of(reportService.listByAlert(alertId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar relatório por ID")
    public ResponseEntity<SuccessResponse<ReportResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(reportService.findById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONDER')")
    @Operation(summary = "Atualizar status do relatório",
               description = "ADMIN ou RESPONDER podem verificar ou rejeitar relatórios de campo")
    public ResponseEntity<SuccessResponse<ReportResponse>> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "Novo status: PENDING, VERIFIED, REJECTED")
            @RequestParam AlertReport.ReportStatus status
    ) {
        return ResponseEntity.ok(SuccessResponse.of("Status atualizado", reportService.updateStatus(id, status)));
    }
}
