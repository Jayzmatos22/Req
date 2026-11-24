package estudo.requisicoes.req.controller.httpClient;

import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.service.httpClient.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/httpclient/users")
public class UserControllerHttp {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }
}
