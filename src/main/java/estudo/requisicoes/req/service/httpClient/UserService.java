package estudo.requisicoes.req.service.httpClient;

import estudo.requisicoes.req.clientes.JavaHttpClientExemplo;
import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.ExternalApiException;
import estudo.requisicoes.req.exception.PageNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JavaHttpClientExemplo clientHttp;
    private final JsonMapper jsonMapper;

    public List<UserDTO> listAllUsers(int page, int limit){
        HttpResponse<String> response = clientHttp.get("/users", Map.of("_page", page, "_limit", limit));
        if (response.statusCode() >= 500){
            throw new ExternalApiException("API falhou: " + response.statusCode());
        }

        List<UserDTO> users = jsonMapper.readValue(response.body(), new TypeReference<List<UserDTO>>() {});

        if (users.isEmpty()){
            throw new PageNotFoundException(page, limit);
        }
        return users;

    }
}
