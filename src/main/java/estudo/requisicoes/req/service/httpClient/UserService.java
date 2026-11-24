package estudo.requisicoes.req.service.httpClient;

import estudo.requisicoes.req.clientes.JavaHttpClientExemplo;
import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JavaHttpClientExemplo clientHttp;
    private final JsonMapper jsonMapper;

    public List<UserDTO> listAllUsers(){
        HttpResponse<String> response = clientHttp.get("/users");
        if (response.statusCode() >= 500){
            throw new ExternalApiException("API falhou: " + response.statusCode());
        }
        return jsonMapper.readValue(response.body(), new TypeReference<List<UserDTO>>() {
        });

    }
}
