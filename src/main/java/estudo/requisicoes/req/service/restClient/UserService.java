package estudo.requisicoes.req.service.restClient;

import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.ExternalApiException;
import estudo.requisicoes.req.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final RestClient restClient;

    // com paginação
    public List<UserDTO> listUsers(int page, int limit){
        List<UserDTO> response = restClient.get()
                .uri(uri -> uri.path("/users")
                        .queryParam("_page", page)
                        .queryParam("_limit", limit)
                        .build())
                .header("accept", "application/json")
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, ((req, res) -> {
                    throw new ExternalApiException("API de usuários falhou: " + res.getStatusCode());
                }))
                .body(new ParameterizedTypeReference<List<UserDTO>>() {
                });

        return response;
    }
}
