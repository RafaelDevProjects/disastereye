package br.com.fiap.disastereye.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ReportRequest {

    public record CreateReportRequest(
            @NotNull(message = "ID do alerta é obrigatório")
            Long alertId,

            @NotBlank(message = "Descrição é obrigatória")
            String description,

            @Positive(message = "Número de afetados deve ser positivo")
            Integer peopleAffected,

            Boolean infrastructureDamage
    ) {}
}
