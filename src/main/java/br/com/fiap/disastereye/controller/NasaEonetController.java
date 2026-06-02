package br.com.fiap.disastereye.controller;

import br.com.fiap.disastereye.dto.response.ApiResponse.SuccessResponse;
import br.com.fiap.disastereye.service.NasaEonetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/nasa")
@RequiredArgsConstructor
@Tag(name = "NASA EONET")
@SecurityRequirement(name = "BearerAuth")
public class NasaEonetController {

    private final NasaEonetService nasaEonetService;

    @GetMapping("/events")
    @Operation(
        summary = "Eventos ativos da NASA EONET",
        description = """
            Consulta eventos de desastres naturais em tempo real via satélites NASA.
            Retorna dados como incêndios, tempestades, vulcões, secas e outros fenômenos.
            Inclui mecanismo de retry automático com fallback em caso de indisponibilidade.
            """
    )
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getActiveEvents(
            @Parameter(description = "Limite de eventos retornados (1-500)")
            @RequestParam(defaultValue = "50") int limit,

            @Parameter(description = "Categoria EONET (ex: wildfires, floods, volcanoes, severeStorms)")
            @RequestParam(required = false) String category
    ) {
        Map<String, Object> events = nasaEonetService.fetchActiveEvents(limit, category);
        return ResponseEntity.ok(SuccessResponse.of("Eventos NASA EONET", events));
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "Buscar evento NASA por ID",
               description = "Retorna detalhes de um evento específico da NASA EONET incluindo coordenadas de satélite")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getEventById(
            @Parameter(description = "ID do evento NASA EONET (ex: EONET_6589)")
            @PathVariable String eventId
    ) {
        Map<String, Object> event = nasaEonetService.fetchEventById(eventId);
        return ResponseEntity.ok(SuccessResponse.of("Evento NASA", event));
    }

    @GetMapping("/categories")
    @Operation(summary = "Categorias de eventos da NASA EONET",
               description = "Lista todas as categorias de eventos naturais monitorados pelos satélites NASA")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getCategories() {
        Map<String, Object> categories = nasaEonetService.fetchCategories();
        return ResponseEntity.ok(SuccessResponse.of("Categorias NASA EONET", categories));
    }
}
