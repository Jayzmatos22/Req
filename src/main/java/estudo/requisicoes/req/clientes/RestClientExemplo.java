package estudo.requisicoes.req.clientes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientExemplo {

    @Autowired
    private final RestClient restClient;

    // recebe do yaml e repassa na constructor
    public RestClientExemplo(RestClient.Builder builder, @Value("${api.base-url}") String baseUrl){
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }
}
