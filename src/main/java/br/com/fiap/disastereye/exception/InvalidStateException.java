package br.com.fiap.disastereye.exception;

/**
 * Lançada quando uma transição de estado inválida é tentada,
 * ex: tentar reabrir um alerta CANCELLED ou resolver um alerta já RESOLVED.
 */
public class InvalidStateException extends DisasterEyeException {
    private final String currentState;
    private final String attemptedTransition;

    public InvalidStateException(String resource, String currentState, String attemptedTransition) {
        super(String.format(
                "Não é possível realizar '%s' no %s pois ele está em estado '%s'",
                attemptedTransition, resource, currentState
        ));
        this.currentState = currentState;
        this.attemptedTransition = attemptedTransition;
    }

    public String getCurrentState() { return currentState; }
    public String getAttemptedTransition() { return attemptedTransition; }
}
