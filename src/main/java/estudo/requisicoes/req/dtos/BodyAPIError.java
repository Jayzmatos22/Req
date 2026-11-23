package estudo.requisicoes.req.dtos;

import java.time.Instant;
import java.time.LocalDateTime;

public record BodyAPIError(
        int code,
        String errorName,
        String message,
        String uriLocation,
        Instant timestamp
) {
}
