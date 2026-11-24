package estudo.requisicoes.req.clientes;


import estudo.requisicoes.req.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// o controller precisa receber
// vai precisa de JsonMappee tbm
@Component
@RequiredArgsConstructor
public class JavaHttpClientExemplo {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();



    @Value("${api.base-url}")
    private final String baseUrl;

    public HttpRequest buildRequest(String path){
        return HttpRequest.newBuilder()
                .header("accept", "application/json")
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
    }

    public HttpResponse<String> get(String path){
        try{
            return client.send(buildRequest(path),
                    HttpResponse.BodyHandlers.ofString());
        } catch(IOException | InterruptedException e){
            throw new ExternalApiException("Falha ao chamar a API: " + e.getMessage());
        }
    }
}
