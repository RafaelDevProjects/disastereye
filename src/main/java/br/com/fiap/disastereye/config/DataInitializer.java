package br.com.fiap.disastereye.config;

import br.com.fiap.disastereye.model.DisasterAlert;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.repository.DisasterAlertRepository;
import br.com.fiap.disastereye.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DisasterAlertRepository alertRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedAlerts();
        log.info("=== DisasterEye inicializado com dados de exemplo ===");
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@disastereye.com")) {
            userRepository.save(User.builder()
                    .name("Administrador")
                    .email("admin@disastereye.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build());
            log.info("Usuário ADMIN criado: admin@disastereye.com / admin123");
        }

        if (!userRepository.existsByEmail("responder@disastereye.com")) {
            userRepository.save(User.builder()
                    .name("Agente de Resposta")
                    .email("responder@disastereye.com")
                    .password(passwordEncoder.encode("resp123"))
                    .role(User.Role.RESPONDER)
                    .active(true)
                    .build());
            log.info("Usuário RESPONDER criado: responder@disastereye.com / resp123");
        }

        if (!userRepository.existsByEmail("user@disastereye.com")) {
            userRepository.save(User.builder()
                    .name("Usuário Comum")
                    .email("user@disastereye.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(User.Role.USER)
                    .active(true)
                    .build());
            log.info("Usuário USER criado: user@disastereye.com / user123");
        }
    }

    private void seedAlerts() {
        if (alertRepository.count() == 0) {
            // Incêndio no Pantanal
            alertRepository.save(DisasterAlert.builder()
                    .title("Incêndio de Grande Proporção - Pantanal")
                    .description("Foco de incêndio detectado por satélite MODIS/VIIRS em área de preservação.")
                    .type(DisasterAlert.DisasterType.WILDFIRE)
                    .severity(DisasterAlert.SeverityLevel.CRITICAL)
                    .status(DisasterAlert.AlertStatus.ACTIVE)
                    .latitude(-17.7055)
                    .longitude(-57.4213)
                    .location("Pantanal, Mato Grosso do Sul, Brasil")
                    .affectedRadiusKm(45.0)
                    .nasaEventId("EONET_6589")
                    .satelliteSource("NASA MODIS Terra")
                    .detectedAt(LocalDateTime.now().minusHours(3))
                    .build());

            // Inundação no RS
            alertRepository.save(DisasterAlert.builder()
                    .title("Inundação - Vale do Rio dos Sinos")
                    .description("Nível do rio acima do normal. Risco de alagamento em áreas urbanas.")
                    .type(DisasterAlert.DisasterType.FLOOD)
                    .severity(DisasterAlert.SeverityLevel.HIGH)
                    .status(DisasterAlert.AlertStatus.ACTIVE)
                    .latitude(-29.7678)
                    .longitude(-51.1489)
                    .location("São Leopoldo, Rio Grande do Sul, Brasil")
                    .affectedRadiusKm(20.0)
                    .nasaEventId("EONET_6590")
                    .satelliteSource("NASA Sentinel-2")
                    .detectedAt(LocalDateTime.now().minusHours(6))
                    .build());

            // Deslizamento SP
            alertRepository.save(DisasterAlert.builder()
                    .title("Risco de Deslizamento - Serra do Mar")
                    .description("Saturação do solo detectada após chuvas intensas. Alto risco de deslizamento.")
                    .type(DisasterAlert.DisasterType.LANDSLIDE)
                    .severity(DisasterAlert.SeverityLevel.HIGH)
                    .status(DisasterAlert.AlertStatus.MONITORING)
                    .latitude(-23.9618)
                    .longitude(-46.3322)
                    .location("Serra do Mar, São Paulo, Brasil")
                    .affectedRadiusKm(10.0)
                    .satelliteSource("INPE CBERS-4A")
                    .detectedAt(LocalDateTime.now().minusDays(1))
                    .build());

            // Evento resolvido
            alertRepository.save(DisasterAlert.builder()
                    .title("Tempestade Severa - Paraná")
                    .description("Sistema de baixa pressão causou ventos de até 90km/h.")
                    .type(DisasterAlert.DisasterType.STORM)
                    .severity(DisasterAlert.SeverityLevel.MEDIUM)
                    .status(DisasterAlert.AlertStatus.RESOLVED)
                    .latitude(-25.4284)
                    .longitude(-49.2733)
                    .location("Curitiba, Paraná, Brasil")
                    .affectedRadiusKm(30.0)
                    .nasaEventId("EONET_6402")
                    .satelliteSource("NASA GPM")
                    .detectedAt(LocalDateTime.now().minusDays(3))
                    .build());

            log.info("4 alertas de exemplo criados.");
        }
    }
}
