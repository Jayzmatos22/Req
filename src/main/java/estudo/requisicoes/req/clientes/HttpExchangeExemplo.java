package estudo.requisicoes.req.clientes;


import estudo.requisicoes.req.dtos.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/users")
public interface HttpExchangeExemplo {

    @GetExchange("/{id}")
    UserDTO getById(@PathVariable Long id);

    @GetExchange
    List<UserDTO> getListUsers(
            @RequestParam("_page")
            int page, @RequestParam("_limit") int limit);

    @GetExchange
    List<UserDTO> findByEmail(@RequestParam("email") String email);

}
