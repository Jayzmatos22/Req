package estudo.requisicoes.req.service.restClient;

import estudo.requisicoes.req.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final RestClient restClient;

    public List<UserDTO> listUsers(){
        return restClient.get()
                .uri("/users")
                .header("accept", "application/json")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserDTO>>() {});
    }
}
