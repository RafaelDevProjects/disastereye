package br.com.fiap.disastereye.service;

import br.com.fiap.disastereye.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NasaEonetService {

    private final RestTemplate restTemplate;

    @Value("${nasa.api.base-url}")
    private String nasaBaseUrl;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * Busca eventos ativos da API EONET da NASA.
     * Implementa retry com backoff exponencial; lança ExternalServiceException após esgotar tentativas.
     */
    public Map<String, Object> fetchActiveEvents(int limit, String category) {
        String url = buildEventsUrl(limit, category);
        log.info("Consultando NASA EONET: {}", url);
        RestClientException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                log.info("Eventos NASA obtidos com sucesso (tentativa {})", attempt);
                return response != null ? response : Map.of("events", java.util.List.of());
            } catch (RestClientException e) {
                lastException = e;
                log.warn("Tentativa {}/{} falhou ao consultar NASA EONET: {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) sleepWithBackoff(attempt);
            }
        }

        log.error("Todas as {} tentativas falharam ao consultar NASA EONET.", MAX_RETRIES);
        throw new ExternalServiceException("NASA EONET",
                "Não foi possível obter dados da NASA EONET após " + MAX_RETRIES + " tentativas.", lastException);
    }

    /**
     * Busca categorias de eventos disponíveis na NASA EONET.
     */
    public Map<String, Object> fetchCategories() {
        String url = nasaBaseUrl + "/categories";
        log.info("Buscando categorias EONET: {}", url);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null ? response : Map.of("categories", java.util.List.of());
        } catch (RestClientException e) {
            log.error("Falha ao buscar categorias NASA: {}", e.getMessage());
            throw new ExternalServiceException("NASA EONET", "Falha ao buscar categorias da NASA EONET.", e);
        }
    }

    /**
     * Busca um evento específico por ID na NASA EONET.
     */
    public Map<String, Object> fetchEventById(String eventId) {
        String url = nasaBaseUrl + "/events/" + eventId;
        log.info("Buscando evento NASA [id={}]", eventId);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null ? response : Map.of();
        } catch (RestClientException e) {
            log.error("Erro ao buscar evento NASA [id={}]: {}", eventId, e.getMessage());
            throw new ExternalServiceException("NASA EONET",
                    "Evento NASA não encontrado ou serviço indisponível: " + eventId, e);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String buildEventsUrl(int limit, String category) {
        StringBuilder url = new StringBuilder(nasaBaseUrl)
                .append("/events?status=open&limit=").append(limit);
        if (category != null && !category.isBlank()) {
            url.append("&category=").append(category);
        }
        return url.toString();
    }

    private void sleepWithBackoff(int attempt) {
        try {
            long delay = RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
            log.debug("Aguardando {}ms antes da próxima tentativa...", delay);
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
