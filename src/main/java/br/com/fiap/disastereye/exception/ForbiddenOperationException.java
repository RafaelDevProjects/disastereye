package br.com.fiap.disastereye.exception;

/**
 * Lançada quando uma operação é proibida por regra de negócio,
 * distinta do AccessDeniedException que trata segurança de role/permissão.
 *
 * Exemplos: tentar fechar um alerta que já está resolvido,
 * editar um relatório que não pertence ao usuário.
 */
public class ForbiddenOperationException extends DisasterEyeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
