package br.com.fiap.disastereye.dto.request;

import br.com.fiap.disastereye.model.DisasterAlert;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class AlertRequest {

    public record CreateAlertRequest(
            @NotBlank(message = "Título é obrigatório")
            @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
            String title,

            String description,

            @NotNull(message = "Tipo do desastre é obrigatório")
            DisasterAlert.DisasterType type,

            @NotNull(message = "Severidade é obrigatória")
            DisasterAlert.SeverityLevel severity,

            @NotNull(message = "Latitude é obrigatória")
            @DecimalMin(value = "-90.0", message = "Latitude inválida")
            @DecimalMax(value = "90.0", message = "Latitude inválida")
            Double latitude,

            @NotNull(message = "Longitude é obrigatória")
            @DecimalMin(value = "-180.0", message = "Longitude inválida")
            @DecimalMax(value = "180.0", message = "Longitude inválida")
            Double longitude,

            @NotBlank(message = "Localização é obrigatória")
            String location,

            @Positive(message = "Raio afetado deve ser positivo")
            Double affectedRadiusKm,

            String nasaEventId,

            String satelliteSource,

            LocalDateTime detectedAt
    ) {}

    public record UpdateAlertRequest(
            String title,
            String description,
            DisasterAlert.SeverityLevel severity,
            DisasterAlert.AlertStatus status,
            Double affectedRadiusKm
    ) {}

    public record NearbyAlertsRequest(
            @NotNull(message = "Latitude é obrigatória")
            @DecimalMin(value = "-90.0")
            @DecimalMax(value = "90.0")
            Double latitude,

            @NotNull(message = "Longitude é obrigatória")
            @DecimalMin(value = "-180.0")
            @DecimalMax(value = "180.0")
            Double longitude,

            @Positive(message = "Raio deve ser positivo")
            Double radiusKm
    ) {}
}
