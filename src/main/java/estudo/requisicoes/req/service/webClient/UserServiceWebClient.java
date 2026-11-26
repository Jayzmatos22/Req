package estudo.requisicoes.req.service.webClient;

import estudo.requisicoes.req.clientes.WebClientExemplo;
import estudo.requisicoes.req.dtos.UserDTO;
import estudo.requisicoes.req.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceWebClient {
    private final WebClient webClient;

    public UserDTO getById(Long id){
        System.out.println("[BLOCK] 1. entrou no service      -> " + Thread.currentThread().getName());

        UserDTO user = webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                // onStatus so tem response como parametro
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new UserNotFoundException(id)))
                .bodyToMono(UserDTO.class)
                // com .delayElement(Duration.ofSeconds(5)) 2° thread espera a primeira
                .doOnNext(u -> System.out.println("[BLOCK] 2. resposta chegou       -> " + Thread.currentThread().getName()))
                .block(); // bloqueia thread

        System.out.println("[BLOCK] 3. saiu do .block()       -> " + Thread.currentThread().getName());
        return user;
    }

    public Mono<UserDTO> getByIdMono(Long id){
        System.out.println("[MONO]  1. entrou no service      -> " + Thread.currentThread().getName());

        Mono<UserDTO> mono = webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new UserNotFoundException(id)))
                .bodyToMono(UserDTO.class) // sem bloqueio
                .doOnNext(u -> System.out.println("[MONO]  3. resposta chegou       -> " + Thread.currentThread().getName()));

        System.out.println("[MONO]  2. devolveu o Mono (nada enviado ainda) -> " + Thread.currentThread().getName());
        return mono;
    }
}
