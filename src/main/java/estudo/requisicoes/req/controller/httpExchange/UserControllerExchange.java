package estudo.requisicoes.req.controller.httpExchange;


import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.service.httpExchange.UserServiceExchange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange/users")
public class UserControllerExchange {

    private final UserServiceExchange userServiceExchange;

    @GetMapping
    public ResponseEntity<List<UserDTO>> listUsers(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int limit
    ) {
        return ResponseEntity.ok(userServiceExchange.listUsers(page, limit));
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserDTO> findByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(userServiceExchange.findByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userServiceExchange.getById(id));
    }
}
