package estudo.requisicoes.req.clientes;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

// o controller precisa receber
// vai precisa de JsonMappee tbm
@Service
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
}
