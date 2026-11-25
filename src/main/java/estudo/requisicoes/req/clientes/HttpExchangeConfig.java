package estudo.requisicoes.req.clientes;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpExchangeConfig {

    @Bean
    public HttpExchangeExemplo httpExchangeExemplo(
            RestClient.Builder builder,
            @Value("${api.base-url}") String baseUrl) {
        RestClient restClient = builder.baseUrl(baseUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter).build();

        return factory.createClient(HttpExchangeExemplo.class);
    }
}
