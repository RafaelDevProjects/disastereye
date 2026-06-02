package br.com.fiap.disastereye.exception;

public class ResourceNotFoundException extends DisasterEyeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " não encontrado(a) com id: " + id);
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
