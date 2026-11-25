package estudo.requisicoes.req.service.UserService;

import estudo.requisicoes.req.clientes.RestTemplateExemplo;
import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class restTemplate {
    private final RestTemplate restTemplate;

    public UserDTO getByID(Long id){
        try {
            return restTemplate.getForObject("/users/{id}", UserDTO.class, id);
        } catch (HttpClientErrorException e) {
            throw new UserNotFoundException(id);
        }
    }
}
