package estudo.requisicoes.req.service.httpUrlConnection;

import estudo.requisicoes.req.clientes.HttpUrlConnectionExemplo;
import estudo.requisicoes.req.dtos.RespostaHttp;
import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import tools.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
public class UserResponseHttpUrl {
    private final HttpUrlConnectionExemplo httpUrlConnectionExemplo;
    private final JsonMapper jsonMapper;

    public List<UserDTO> listUser(){
        RespostaHttp response = httpUrlConnectionExemplo.get("/users");
        if (response.status() >= 500){
            throw new ExternalApiException("API falhou: " + response.status());
        }

        return jsonMapper.readValue(response.body(), new TypeReference<List<UserDTO>>() {});
    }
}
