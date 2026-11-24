package estudo.requisicoes.req.exception;

import estudo.requisicoes.req.dtos.BodyAPIError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class Global {

    // corpo da exception
    private ResponseEntity<BodyAPIError> build(
            HttpStatus status, String error, String message,
            HttpServletRequest req) {
        return ResponseEntity.status(status).body(new BodyAPIError(
                status.value(), error, message, req.getRequestURI(), Instant.now()));
    }

    @ExceptionHandler
    public ResponseEntity<BodyAPIError> handlerUserNotFound(UserNotFoundException ex, HttpServletRequest req){
        return build(HttpStatus.NOT_FOUND, "Usuário não encontrado", ex.getMessage(), req);
    }

    @ExceptionHandler
    public ResponseEntity<BodyAPIError> handlerServerError(Exception ex, HttpServletRequest req){
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro no servidor",
                ex.getMessage(),
                req);
    }

    @ExceptionHandler
    public ResponseEntity<BodyAPIError> handlerEmailNotFound(EmailNotFoundException ex, HttpServletRequest req){
        return build(
                HttpStatus.NOT_FOUND,
                "Email não encontrado",
                ex.getMessage(),
                req);
    }

    @ExceptionHandler
    public ResponseEntity<BodyAPIError> handlerEmailSearch(SearchEmailException ex, HttpServletRequest req){
        return build(
                HttpStatus.BAD_REQUEST,
                "Email inválido",
                ex.getMessage(),
                req);
    }

    @ExceptionHandler
    public ResponseEntity<BodyAPIError> handlerPageNotFound(PageNotFoundException ex, HttpServletRequest req){
        return build(
                HttpStatus.NOT_FOUND,
                "Limite de páginas",
                ex.getMessage(),
                req);
    }



}
