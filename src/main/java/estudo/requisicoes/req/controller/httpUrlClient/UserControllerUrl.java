package estudo.requisicoes.req.controller.httpUrlClient;

import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.service.httpUrlConnection.UserServiceHttpUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/httpurl/users")
@RestController
@RequiredArgsConstructor
public class UserControllerUrl {
    private final UserServiceHttpUrl userServiceHttpUrl;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userServiceHttpUrl.listUser());
    }
}
