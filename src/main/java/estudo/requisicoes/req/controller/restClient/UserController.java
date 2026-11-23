package estudo.requisicoes.req.controller.restClient;

import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.service.restClient.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(userService.listUsers(page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<UserDTO> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }


}
