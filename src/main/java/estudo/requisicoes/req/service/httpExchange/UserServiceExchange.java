package estudo.requisicoes.req.service.httpExchange;

import estudo.requisicoes.req.clientes.HttpExchangeExemplo;
import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.EmailNotFoundException;
import estudo.requisicoes.req.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceExchange {
    private final HttpExchangeExemplo api;

    public UserDTO getById(Long id){
        try{
            return api.getById(id);
        } catch (HttpClientErrorException.NotFound e){
            throw new UserNotFoundException(id);
        }
    }

    public List<UserDTO> listUsers(int page, int limit) {
        return api.getListUsers(page, limit);
    }

    public UserDTO findByEmail(String email) {
        List<UserDTO> users = api.findByEmail(email.trim()); // interface devolve lista
        if (users.isEmpty()) {
            throw new EmailNotFoundException(email);        // regra: vazio = não achou
        }
        return users.getFirst();
    }
}
