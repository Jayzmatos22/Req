package estudo.requisicoes.req.service.apacheHttpClient5;


import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.ExternalApiException;
import estudo.requisicoes.req.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserServiceApacheClient {
    private final CloseableHttpClient httpClient;
    private final JsonMapper jsonMapper;

    @Value("${api.base-url}")
    private String baseUrl;

    public UserDTO getByID(Long id){
        HttpGet request = new HttpGet(baseUrl + "/users/" + id);

        try {
            String body = httpClient.execute(request, response -> {
                int status = response.getCode();
                if (status == 404) {
                    throw new UserNotFoundException(id);
                }

                if (status >= 500) {
                    throw new ExternalApiException("API falhou: " + status);
                }
                return EntityUtils.toString(response.getEntity());
            });

            return jsonMapper.readValue(body, UserDTO.class);

            } catch (IOException e) {
            throw new ExternalApiException("Falha ao chamar a API: " + e.getMessage());
        }
    }
}