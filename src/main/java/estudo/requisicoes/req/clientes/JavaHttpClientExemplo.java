package estudo.requisicoes.req.clientes;


import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.time.Duration;

// o controller precisa receber
// vai precisa de JsonMappee tbm
@Service
public class JavaHttpClientExemplo {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
}
