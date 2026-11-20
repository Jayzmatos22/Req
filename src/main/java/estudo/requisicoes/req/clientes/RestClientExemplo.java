package estudo.requisicoes.req.clientes;

import estudo.requisicoes.req.dtos.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class RestClientExemplo {

    // recebe do yaml e repassa na constructor
    @Bean
    public RestClient restClient(RestClient.Builder builder, @Value("${api.base-url}") String baseUrl){
        return builder.baseUrl(baseUrl).build();
    }

}
