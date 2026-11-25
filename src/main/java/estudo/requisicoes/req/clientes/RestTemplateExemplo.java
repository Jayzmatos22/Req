package estudo.requisicoes.req.clientes;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RestTemplateExemplo {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${api.base-url}") String baseUrl) {
        // monta e usa as mesmas config onde for injetado
        return builder.rootUri(baseUrl)
                .defaultHeader("Accept", "application/json")
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }
}
