package estudo.requisicoes.req.controller.apacheHttp;


import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.service.apacheHttpClient5.UserServiceApacheClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apache/users")
public class UserControllerApache {
    private final UserServiceApacheClient userServiceApacheClient;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userServiceApacheClient.getByID(id));
    }

}
