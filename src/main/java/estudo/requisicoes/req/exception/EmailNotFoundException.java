package estudo.requisicoes.req.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super("Email não existe: "+email);
    }
}
