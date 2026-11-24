package estudo.requisicoes.req.clientes;


import estudo.requisicoes.req.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

// o controller precisa receber
// vai precisa de JsonMappee tbm
@Component
public class JavaHttpClientExemplo {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();


    private final String baseUrl;

    public JavaHttpClientExemplo(@Value("${api.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private HttpRequest buildRequest(URI uri){
        return HttpRequest.newBuilder()
                .header("accept", "application/json")
                .uri(uri)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
    }

    public HttpResponse<String> get(String path, Map<String, ?> queryParams){
        try{
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(path);
            queryParams.forEach(builder::queryParam);

            URI uri = builder.encode().build().toUri();

            return client.send(buildRequest(uri),
                    HttpResponse.BodyHandlers.ofString());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // restaura a flag
            throw new ExternalApiException("Chamada interrompida "+ e);
        } catch (IOException e) {
            throw new ExternalApiException("Falha ao chamar a API "+ e);
        }
    }
}
