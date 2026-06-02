package br.com.fiap.disastereye.controller;

import br.com.fiap.disastereye.dto.response.ApiResponse.SuccessResponse;
import br.com.fiap.disastereye.dto.response.ApiResponse.UserResponse;
import br.com.fiap.disastereye.dto.response.PageResponse;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuários")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Listar todos os usuários", description = "Apenas ADMIN")
    public ResponseEntity<SuccessResponse<PageResponse<UserResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(SuccessResponse.of(userService.listAll(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<SuccessResponse<UserResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(userService.findById(id)));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Alterar role do usuário",
               description = "Permite ADMIN promover/rebaixar usuários entre USER, RESPONDER e ADMIN")
    public ResponseEntity<SuccessResponse<UserResponse>> updateRole(
            @PathVariable Long id,
            @Parameter(description = "Nova role: USER, RESPONDER, ADMIN")
            @RequestParam User.Role role
    ) {
        return ResponseEntity.ok(SuccessResponse.of("Role atualizada", userService.updateRole(id, role)));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Ativar/desativar usuário",
               description = "Alterna o estado ativo/inativo de um usuário")
    public ResponseEntity<SuccessResponse<UserResponse>> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of("Estado do usuário atualizado", userService.toggleActive(id)));
    }
}
