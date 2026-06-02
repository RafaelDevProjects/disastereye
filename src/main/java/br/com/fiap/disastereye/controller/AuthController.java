package br.com.fiap.disastereye.controller;

import br.com.fiap.disastereye.dto.request.AuthRequest.LoginRequest;
import br.com.fiap.disastereye.dto.request.AuthRequest.RegisterRequest;
import br.com.fiap.disastereye.dto.response.ApiResponse.AuthResponse;
import br.com.fiap.disastereye.dto.response.ApiResponse.SuccessResponse;
import br.com.fiap.disastereye.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário",
               description = "Cria uma nova conta de usuário e retorna o token JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "E-mail já cadastrado")
    })
    public ResponseEntity<SuccessResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse auth = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("Usuário registrado com sucesso", auth));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário",
               description = "Autentica credenciais e retorna token JWT para uso nos demais endpoints")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<SuccessResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(SuccessResponse.of("Login realizado com sucesso", auth));
    }
}
