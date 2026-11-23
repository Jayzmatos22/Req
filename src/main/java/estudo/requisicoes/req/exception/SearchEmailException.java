package estudo.requisicoes.req.exception;

public class SearchEmailException extends RuntimeException {
    public SearchEmailException(String email) {
        super("Email inválido: "+email);
    }
}
