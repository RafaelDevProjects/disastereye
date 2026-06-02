package br.com.fiap.disastereye.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ─────────────────────────────────────────────────────────────────────────
    // Estrutura de resposta de erro padronizada
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Corpo de resposta de erro.
     *
     * @param success     sempre false em erros
     * @param status      HTTP status code numérico
     * @param errorCode   código legível por máquina (ex: RESOURCE_NOT_FOUND)
     * @param errorType   categoria: BUSINESS | VALIDATION | AUTHENTICATION |
     *                    AUTHORIZATION | INTEGRATION | TECHNICAL
     * @param message     mensagem amigável para o usuário final
     * @param details     mapa de detalhes adicionais (campos inválidos, estado atual, etc.)
     * @param path        URI que gerou o erro
     * @param timestamp   momento exato do erro
     */
    public record ErrorResponse(
            boolean success,
            int status,
            String errorCode,
            String errorType,
            String message,
            Map<String, Object> details,
            String path,
            LocalDateTime timestamp
    ) {
        static ErrorResponse of(
                HttpStatus httpStatus,
                String errorCode,
                String errorType,
                String message,
                Map<String, Object> details,
                String path
        ) {
            return new ErrorResponse(
                    false,
                    httpStatus.value(),
                    errorCode,
                    errorType,
                    message,
                    details,
                    path,
                    LocalDateTime.now()
            );
        }
    }

    private ResponseEntity<ErrorResponse> respond(
            HttpStatus status,
            String errorCode,
            String errorType,
            String message,
            Map<String, Object> details,
            String path
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status, errorCode, errorType, message, details, path));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Erros de NEGÓCIO (regras da aplicação)
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("[BUSINESS] Recurso não encontrado | path={} | msg={}", req.getRequestURI(), ex.getMessage());
        return respond(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "BUSINESS",
                ex.getMessage(),
                null,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest req) {
        log.warn("[BUSINESS] Conflito de recurso | path={} | msg={}", req.getRequestURI(), ex.getMessage());

        Map<String, Object> details = null;
        if (ex.getField() != null) {
            details = Map.of("field", ex.getField(), "value", String.valueOf(ex.getValue()));
        }

        return respond(
                HttpStatus.CONFLICT,
                "RESOURCE_CONFLICT",
                "BUSINESS",
                ex.getMessage(),
                details,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
            InvalidStateException ex, HttpServletRequest req) {
        log.warn("[BUSINESS] Transição de estado inválida | path={} | currentState={} | attempted={}",
                req.getRequestURI(), ex.getCurrentState(), ex.getAttemptedTransition());
        return respond(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "INVALID_STATE_TRANSITION",
                "BUSINESS",
                ex.getMessage(),
                Map.of(
                        "currentState", ex.getCurrentState(),
                        "attemptedTransition", ex.getAttemptedTransition()
                ),
                req.getRequestURI()
        );
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenOperation(
            ForbiddenOperationException ex, HttpServletRequest req) {
        log.warn("[BUSINESS] Operação proibida por regra de negócio | path={} | msg={}",
                req.getRequestURI(), ex.getMessage());
        return respond(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN_OPERATION",
                "BUSINESS",
                ex.getMessage(),
                null,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest req) {
        log.warn("[BUSINESS] Erro de negócio | path={} | msg={}", req.getRequestURI(), ex.getMessage());
        return respond(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "BUSINESS_RULE_VIOLATION",
                "BUSINESS",
                ex.getMessage(),
                null,
                req.getRequestURI()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Erros de VALIDAÇÃO (entrada de dados)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Erros de @Valid / @Validated em @RequestBody.
     * Sobrescreve o método do ResponseEntityExceptionHandler para manter
     * o formato padronizado do projeto.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> fieldErrors = new LinkedHashMap<>();

        // Erros de campo
        ex.getBindingResult().getFieldErrors().forEach((FieldError fe) ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage())
        );

        // Erros de objeto (class-level constraints)
        List<String> globalErrors = ex.getBindingResult().getGlobalErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());

        if (!globalErrors.isEmpty()) {
            fieldErrors.put("_global", globalErrors);
        }

        log.warn("[VALIDATION] Campos inválidos | fields={}", fieldErrors.keySet());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "VALIDATION",
                "Um ou mais campos possuem valores inválidos. Corrija e tente novamente.",
                fieldErrors,
                extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Parâmetro de query obrigatório ausente (@RequestParam sem defaultValue).
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("[VALIDATION] Parâmetro ausente | param={}", ex.getParameterName());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "MISSING_PARAMETER",
                "VALIDATION",
                "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'",
                Map.of("parameter", ex.getParameterName(), "type", ex.getParameterType()),
                extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Tipo incompatível no parâmetro (ex: texto onde se espera Long/Enum).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "desconhecido";

        log.warn("[VALIDATION] Tipo inválido no parâmetro | param={} | value={} | expected={}",
                ex.getName(), ex.getValue(), expectedType);

        return respond(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER_TYPE",
                "VALIDATION",
                String.format(
                        "O parâmetro '%s' recebeu o valor '%s', mas esperava um '%s' válido.",
                        ex.getName(), ex.getValue(), expectedType
                ),
                Map.of(
                        "parameter", ex.getName(),
                        "receivedValue", String.valueOf(ex.getValue()),
                        "expectedType", expectedType
                ),
                req.getRequestURI()
        );
    }

    /**
     * JSON malformado ou corpo da requisição ilegível.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("[VALIDATION] Corpo da requisição ilegível | cause={}", ex.getMessage());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST_BODY",
                "VALIDATION",
                "O corpo da requisição está mal formatado ou contém valores inválidos. "
                        + "Verifique se o JSON está correto e os tipos dos campos.",
                null,
                extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Violação de constraint no banco de dados (unique, not-null, FK).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest req) {

        String rootMsg = ex.getRootCause() != null
                ? ex.getRootCause().getMessage()
                : ex.getMessage();

        boolean isUnique = rootMsg != null &&
                (rootMsg.toLowerCase().contains("unique") || rootMsg.toLowerCase().contains("duplicate"));

        log.warn("[TECHNICAL] Violação de integridade no banco | unique={} | cause={}", isUnique, rootMsg);

        String userMessage = isUnique
                ? "Já existe um registro com os dados informados. Verifique os campos únicos."
                : "Operação viola uma restrição de integridade dos dados.";

        return respond(
                HttpStatus.CONFLICT,
                isUnique ? "UNIQUE_CONSTRAINT_VIOLATION" : "DATA_INTEGRITY_VIOLATION",
                "TECHNICAL",
                userMessage,
                null,
                req.getRequestURI()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Erros de AUTENTICAÇÃO / AUTORIZAÇÃO
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        // Mensagem genérica proposital — não revelar qual campo está errado
        log.warn("[AUTH] Credenciais inválidas | path={}", req.getRequestURI());
        return respond(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "AUTHENTICATION",
                "E-mail ou senha inválidos.",
                null,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledUser(
            DisabledException ex, HttpServletRequest req) {
        log.warn("[AUTH] Tentativa de login com conta desativada | path={}", req.getRequestURI());
        return respond(
                HttpStatus.UNAUTHORIZED,
                "ACCOUNT_DISABLED",
                "AUTHENTICATION",
                "Esta conta está desativada. Entre em contato com o suporte.",
                null,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedUser(
            LockedException ex, HttpServletRequest req) {
        log.warn("[AUTH] Conta bloqueada | path={}", req.getRequestURI());
        return respond(
                HttpStatus.UNAUTHORIZED,
                "ACCOUNT_LOCKED",
                "AUTHENTICATION",
                "Esta conta está bloqueada temporariamente. Tente novamente mais tarde.",
                null,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest req) {
        log.warn("[AUTH] Falha de autenticação | path={} | cause={}", req.getRequestURI(), ex.getClass().getSimpleName());
        return respond(
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_FAILED",
                "AUTHENTICATION",
                "Autenticação necessária. Faça login para continuar.",
                null,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        log.warn("[AUTH] Acesso negado | path={} | method={}", req.getRequestURI(), req.getMethod());
        return respond(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "AUTHORIZATION",
                "Você não tem permissão para realizar esta operação.",
                null,
                req.getRequestURI()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Erros de INTEGRAÇÃO com serviços externos
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(
            ExternalServiceException ex, HttpServletRequest req) {
        log.error("[INTEGRATION] Falha na integração | service={} | path={} | msg={}",
                ex.getServiceName(), req.getRequestURI(), ex.getMessage());
        return respond(
                HttpStatus.BAD_GATEWAY,
                "EXTERNAL_SERVICE_UNAVAILABLE",
                "INTEGRATION",
                String.format(
                        "O serviço externo '%s' está temporariamente indisponível. Tente novamente mais tarde.",
                        ex.getServiceName()
                ),
                Map.of("service", ex.getServiceName()),
                req.getRequestURI()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Erros de INFRAESTRUTURA / HTTP  (Spring Boot 3 / Spring 6)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Rota estática não encontrada — Spring Boot 3 lança NoResourceFoundException
     * em vez de NoHandlerFoundException para recursos estáticos.
     */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("[HTTP] Rota não encontrada | method={} | path={}", ex.getHttpMethod(), ex.getResourcePath());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                "ROUTE_NOT_FOUND",
                "TECHNICAL",
                String.format("A rota '%s /%s' não existe.", ex.getHttpMethod(), ex.getResourcePath()),
                null,
                extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Content-Type não suportado (ex: envio de XML onde se espera JSON).
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String supported = ex.getSupportedMediaTypes().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        log.warn("[HTTP] Content-Type não suportado | received={} | supported={}", ex.getContentType(), supported);

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "TECHNICAL",
                String.format(
                        "Content-Type '%s' não é suportado. Use: %s.",
                        ex.getContentType(), supported
                ),
                Map.of("supportedTypes", supported),
                extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("[TECHNICAL] Argumento inválido | path={} | msg={}", req.getRequestURI(), ex.getMessage());
        return respond(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT",
                "TECHNICAL",
                ex.getMessage(),
                null,
                req.getRequestURI()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Fallback genérico (qualquer exceção não capturada acima)
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest req) {
        // Stack trace completo no log — nunca exposto ao cliente
        log.error("[TECHNICAL] Erro inesperado não tratado | path={} | exceptionType={}",
                req.getRequestURI(), ex.getClass().getName(), ex);

        return respond(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "TECHNICAL",
                "Ocorreu um erro interno inesperado. Nossa equipe foi notificada. Tente novamente em instantes.",
                null,
                req.getRequestURI()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String extractPath(WebRequest request) {
        String desc = request.getDescription(false);
        return desc.startsWith("uri=") ? desc.substring(4) : desc;
    }
}