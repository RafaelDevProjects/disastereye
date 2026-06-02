package br.com.fiap.disastereye.dto.response;

import br.com.fiap.disastereye.model.DisasterAlert;
import br.com.fiap.disastereye.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    // ─── Generic wrapper ───────────────────────────────────────────────────────

    @Builder
    public record SuccessResponse<T>(
            boolean success,
            String message,
            T data,
            LocalDateTime timestamp
    ) {
        public static <T> SuccessResponse<T> of(T data) {
            return SuccessResponse.<T>builder()
                    .success(true)
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> SuccessResponse<T> of(String message, T data) {
            return SuccessResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // ─── Auth ──────────────────────────────────────────────────────────────────

    public record AuthResponse(
            String token,
            String type,
            String email,
            String name,
            User.Role role,
            LocalDateTime expiresAt
    ) {
        public static AuthResponse of(String token, User user, LocalDateTime expiresAt) {
            return new AuthResponse(token, "Bearer", user.getEmail(), user.getName(), user.getRole(), expiresAt);
        }
    }

    // ─── User ──────────────────────────────────────────────────────────────────

    public record UserResponse(
            Long id,
            String name,
            String email,
            User.Role role,
            boolean active,
            LocalDateTime createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(), user.getName(), user.getEmail(),
                    user.getRole(), user.isActive(), user.getCreatedAt()
            );
        }
    }

    // ─── Alert ─────────────────────────────────────────────────────────────────

    public record AlertResponse(
            Long id,
            String title,
            String description,
            DisasterAlert.DisasterType type,
            DisasterAlert.SeverityLevel severity,
            DisasterAlert.AlertStatus status,
            Double latitude,
            Double longitude,
            String location,
            Double affectedRadiusKm,
            String nasaEventId,
            String satelliteSource,
            LocalDateTime detectedAt,
            LocalDateTime createdAt
    ) {
        public static AlertResponse from(DisasterAlert alert) {
            return new AlertResponse(
                    alert.getId(), alert.getTitle(), alert.getDescription(),
                    alert.getType(), alert.getSeverity(), alert.getStatus(),
                    alert.getLatitude(), alert.getLongitude(), alert.getLocation(),
                    alert.getAffectedRadiusKm(), alert.getNasaEventId(), alert.getSatelliteSource(),
                    alert.getDetectedAt(), alert.getCreatedAt()
            );
        }
    }

    // ─── Statistics ────────────────────────────────────────────────────────────

    public record DashboardStatsResponse(
            long totalAlerts,
            long activeAlerts,
            long resolvedAlerts,
            long criticalAlerts,
            Map<String, Long> alertsByType
    ) {}

    // ─── NASA EONET (passthrough) ──────────────────────────────────────────────

    public record NasaEventResponse(
            String id,
            String title,
            String description,
            List<Map<String, Object>> categories,
            List<Map<String, Object>> geometry
    ) {}
}
