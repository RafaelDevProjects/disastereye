package br.com.fiap.disastereye.exception;

/**
 * Lançada quando há tentativa de criar um recurso que já existe
 * (ex: e-mail duplicado, evento NASA já importado).
 */
public class ConflictException extends DisasterEyeException {
    private final String field;
    private final Object value;

    public ConflictException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    public ConflictException(String field, Object value) {
        super("Já existe um registro com " + field + " = '" + value + "'");
        this.field = field;
        this.value = value;
    }

    public String getField() { return field; }
    public Object getValue() { return value; }
}
