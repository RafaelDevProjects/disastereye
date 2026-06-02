package br.com.fiap.disastereye.controller;

import br.com.fiap.disastereye.dto.request.AlertRequest.*;
import br.com.fiap.disastereye.dto.response.ApiResponse.*;
import br.com.fiap.disastereye.dto.response.PageResponse;
import br.com.fiap.disastereye.model.DisasterAlert;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.service.DisasterAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas")
@SecurityRequirement(name = "BearerAuth")
public class DisasterAlertController {

    private final DisasterAlertService alertService;

    // ─── GET /api/v1/alerts ───────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar alertas",
               description = "Retorna lista paginada de alertas. Suporta filtro por status, tipo e severidade.")
    public ResponseEntity<SuccessResponse<PageResponse<AlertResponse>>> listAlerts(
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) DisasterAlert.AlertStatus status,
            @Parameter(description = "Filtrar por tipo")   @RequestParam(required = false) DisasterAlert.DisasterType type,
            @Parameter(description = "Filtrar por severidade") @RequestParam(required = false) DisasterAlert.SeverityLevel severity,
            @Parameter(description = "Número da página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(SuccessResponse.of(alertService.listAlerts(status, type, severity, page, size)));
    }

    // ─── GET /api/v1/alerts/{id} ──────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Buscar alerta por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerta encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public ResponseEntity<SuccessResponse<AlertResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(alertService.findById(id)));
    }

    // ─── GET /api/v1/alerts/nearby ────────────────────────────────────────────

    @GetMapping("/nearby")
    @Operation(summary = "Buscar alertas próximos",
               description = "Retorna alertas ativos dentro de um raio (km) de uma coordenada geográfica")
    public ResponseEntity<SuccessResponse<List<AlertResponse>>> findNearby(
            @Parameter(description = "Latitude", required = true)  @RequestParam Double lat,
            @Parameter(description = "Longitude", required = true) @RequestParam Double lon,
            @Parameter(description = "Raio em km (padrão: 100)") @RequestParam(defaultValue = "100") Double radiusKm
    ) {
        return ResponseEntity.ok(SuccessResponse.of(alertService.findNearby(lat, lon, radiusKm)));
    }

    // ─── POST /api/v1/alerts ──────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONDER')")
    @Operation(summary = "Criar novo alerta",
               description = "Cria um alerta de desastre. Requer role ADMIN ou RESPONDER.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Alerta criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<SuccessResponse<AlertResponse>> create(
            @Valid @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        AlertResponse created = alertService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Alerta criado com sucesso", created));
    }

    // ─── PUT /api/v1/alerts/{id} ──────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONDER')")
    @Operation(summary = "Atualizar alerta",
               description = "Atualiza título, descrição, severidade, status ou raio de um alerta existente.")
    public ResponseEntity<SuccessResponse<AlertResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAlertRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of("Alerta atualizado", alertService.update(id, request)));
    }

    // ─── DELETE /api/v1/alerts/{id} ───────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar alerta", description = "Remove um alerta permanentemente. Requer role ADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Alerta deletado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alertService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
