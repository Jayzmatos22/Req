package estudo.requisicoes.req.exception;

public class PageNotFoundException extends RuntimeException {
        public PageNotFoundException(int page, int limit) {
            super("Sem usuários na página " + page + " com limite " + limit);
        }
}
