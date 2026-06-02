package br.com.fiap.disastereye;

import br.com.fiap.disastereye.model.DisasterAlert;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.repository.DisasterAlertRepository;
import br.com.fiap.disastereye.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DisasterEyeApplicationTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired DisasterAlertRepository alertRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Usuários são criados pelo DataInitializer, apenas obtemos tokens
        userToken  = getToken("user@disastereye.com",  "user123");
        adminToken = getToken("admin@disastereye.com", "admin123");
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/register - deve registrar novo usuário")
    void shouldRegisterNewUser() throws Exception {
        var body = Map.of(
                "name", "Novo Usuário",
                "email", "novo@test.com",
                "password", "senha123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("novo@test.com"));
    }

    @Test
    @DisplayName("POST /auth/login - deve autenticar com credenciais válidas")
    void shouldLoginWithValidCredentials() throws Exception {
        var body = Map.of("email", "user@disastereye.com", "password", "user123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("POST /auth/login - deve retornar 401 com credenciais inválidas")
    void shouldReturn401WithInvalidCredentials() throws Exception {
        var body = Map.of("email", "user@disastereye.com", "password", "senhaerrada");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // ─── Alerts ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /alerts - deve listar alertas sem autenticação")
    void shouldListAlertsPublicly() throws Exception {
        mockMvc.perform(get("/api/v1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /alerts - deve filtrar por status ACTIVE")
    void shouldFilterAlertsByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/alerts?status=ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /alerts/nearby - deve retornar alertas próximos")
    void shouldReturnNearbyAlerts() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/nearby?lat=-17.7&lon=-57.4&radiusKm=200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /alerts - deve criar alerta com role ADMIN")
    void shouldCreateAlertAsAdmin() throws Exception {
        var body = Map.of(
                "title", "Incêndio Teste",
                "type", "WILDFIRE",
                "severity", "HIGH",
                "latitude", -15.77,
                "longitude", -47.92,
                "location", "Brasília, DF"
        );

        mockMvc.perform(post("/api/v1/alerts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Incêndio Teste"));
    }

    @Test
    @DisplayName("POST /alerts - deve retornar 403 para USER comum")
    void shouldReturn403ForRegularUser() throws Exception {
        var body = Map.of(
                "title", "Teste",
                "type", "FLOOD",
                "severity", "LOW",
                "latitude", -23.5,
                "longitude", -46.6,
                "location", "São Paulo"
        );

        mockMvc.perform(post("/api/v1/alerts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /dashboard/stats - deve retornar estatísticas")
    void shouldReturnDashboardStats() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAlerts").isNumber())
                .andExpect(jsonPath("$.data.activeAlerts").isNumber());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String getToken(String email, String password) throws Exception {
        var body = Map.of("email", email, "password", password);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
