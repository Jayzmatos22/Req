package estudo.requisicoes.req.controller.restTemplate;

import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.service.UserService.UserServiceTemplate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/template/users")
@RestController
@RequiredArgsConstructor
public class UserControllerRestTemplate {
    private final UserServiceTemplate userServiceTemplate;

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int limit
    ) {
        return ResponseEntity.ok(userServiceTemplate.getList(page, limit));
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getByID(@PathVariable Long id){
        return ResponseEntity.ok(userServiceTemplate.getByID(id));
    }

}
