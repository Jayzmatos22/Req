package estudo.requisicoes.req.dtos;

// Get em users
public record UserDTO(
        Long id,
        String name,
        String username,
        String email
) { }
