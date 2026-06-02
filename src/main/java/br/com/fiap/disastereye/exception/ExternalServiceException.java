package br.com.fiap.disastereye.exception;

/**
 * Lançada quando uma integração com serviço externo (ex: NASA EONET) falha
 * após todas as tentativas de retry.
 */
public class ExternalServiceException extends DisasterEyeException {
    private final String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message);
        initCause(cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
