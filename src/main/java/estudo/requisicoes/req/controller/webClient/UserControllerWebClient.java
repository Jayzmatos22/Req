package estudo.requisicoes.req.controller.webClient;

import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.service.webClient.UserServiceWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/api/webclient/users")
@RestController
public class UserControllerWebClient {
    private final UserServiceWebClient userServiceWebClient;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getByID(@PathVariable Long id) {
        return ResponseEntity.ok(userServiceWebClient.getById(id));
    }

    @GetMapping("/reactive/{id}")
    public Mono<ResponseEntity<UserDTO>> getByIDToMono(@PathVariable Long id) {
        return userServiceWebClient.getByIdMono(id)
                .map(ResponseEntity::ok);
    }
}
