package br.com.fiap.disastereye.controller;

import br.com.fiap.disastereye.dto.response.ApiResponse.DashboardStatsResponse;
import br.com.fiap.disastereye.dto.response.ApiResponse.SuccessResponse;
import br.com.fiap.disastereye.service.DisasterAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private final DisasterAlertService alertService;

    @GetMapping("/stats")
    @Operation(
        summary = "Estatísticas gerais da plataforma",
        description = """
            Retorna métricas consolidadas da plataforma DisasterEye:
            - Total de alertas cadastrados
            - Alertas ativos no momento
            - Alertas resolvidos
            - Alertas críticos
            - Distribuição por tipo de desastre
            """
    )
    public ResponseEntity<SuccessResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(SuccessResponse.of("Estatísticas da plataforma", alertService.getDashboardStats()));
    }
}
