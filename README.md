<h1 align="center">Req — Clientes HTTP em Java e Spring</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4">
  <img src="https://img.shields.io/badge/Spring_Web-MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Web MVC">
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge" alt="Lombok">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/RestClient-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="RestClient">
  <img src="https://img.shields.io/badge/RestTemplate-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="RestTemplate">
  <img src="https://img.shields.io/badge/WebClient-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="WebClient">
  <img src="https://img.shields.io/badge/@HttpExchange-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="HttpExchange">
  <img src="https://img.shields.io/badge/java.net.http.HttpClient-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java HttpClient">
  <img src="https://img.shields.io/badge/HttpURLConnection-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="HttpURLConnection">
  <img src="https://img.shields.io/badge/Apache_HttpClient_5-D22128?style=flat-square&logo=apache&logoColor=white" alt="Apache HttpClient 5">
</p>

<p align="center">
  Estudo das formas de <strong>fazer requisições HTTP</strong> em Java: a mesma chamada,
  implementada com cada cliente, para comparar lado a lado.
</p>

---

## Objetivo

Implementar **as mesmas requisições** com cada cliente HTTP e entender as diferenças entre eles:

- `GET /users?_page=&_limit=` → lista paginada
- `GET /users/{id}` → um usuário (com tratamento de 404)

API alvo: [JSONPlaceholder](https://jsonplaceholder.typicode.com), pública e gratuita.

---

## Clientes estudados

| # | Cliente | Origem | Dependência | Estilo | Converte JSON sozinho? | Status |
|---|---|---|---|---|---|---|
| 1 | **RestClient** | Spring 6.1+ | `spring-boot-starter-restclient` | síncrono, fluente | ✅ | ✅ feito |
| 2 | `java.net.http.HttpClient` | Java 11+ | nenhuma | síncrono ou assíncrono | ❌ (manual) | ⬜ |
| 3 | `HttpURLConnection` | Java 1.1 | nenhuma | baixo nível, verboso | ❌ (manual) | ⬜ |
| 4 | `RestTemplate` | Spring (legado) | `spring-boot-starter-restclient` | síncrono, métodos prontos | ✅ | ⬜ |
| 5 | `WebClient` | Spring WebFlux | `spring-boot-starter-webclient` | reativo (`Mono`/`Flux`) | ✅ | ⬜ |
| 6 | `@HttpExchange` | Spring 6+ | `spring-boot-starter-restclient` | declarativo (só interface) | ✅ | ⬜ |
| 7 | Apache HttpClient 5 | terceiros | `httpclient5` | baixo nível, configurável | ❌ (manual) | ⬜ |

### Critérios de comparação

Ao implementar cada um, observe:

| Critério | Pergunta |
|---|---|
| **Verbosidade** | quantas linhas para o mesmo GET? |
| **JSON** | converte para `UserDTO` sozinho ou precisa de `JsonMapper` na mão? |
| **Erros** | o que acontece num 404/500? lança exceção ou devolve o status para você checar? |
| **Status e headers** | como ler o status e o `X-Total-Count`? |
| **Query params** | tem builder de URL ou é concatenação? |
| **Sync x async** | bloqueia a thread ou devolve uma "promessa"? |

---

## 1. RestClient ✅

O cliente atual e recomendado do Spring para chamadas síncronas.

**Configuração:** um `@Bean` em `clientes/RestClientExemplo` (classe `@Configuration`) cria o `RestClient`
com `baseUrl` vindo do `application.yaml` e o header `Accept` padrão. O `UserService` recebe esse bean pelo construtor.

**Anatomia da cadeia:**

```java
restClient.get()                                   // verbo
        .uri("/users/{id}", id)                    // endereço: path, path variables, query params
        .retrieve()                                // ENVIA a requisição
        .onStatus(s -> s.value() == 404, (req, res) -> {   // reage ao status antes de ler o corpo
            throw new UserNotFoundException(id);
        })
        .body(UserDTO.class);                      // converte o JSON no objeto
```

| Para ler | Use |
|---|---|
| só o corpo | `.body(UserDTO.class)` |
| lista (tipo genérico) | `.body(new ParameterizedTypeReference<List<UserDTO>>() {})` |
| corpo + status + headers | `.toEntity(...)` |
| só status + headers | `.toBodilessEntity()` |

| No `.uri(...)` | Exemplo | Resultado |
|---|---|---|
| caminho fixo | `.uri("/users")` | `/users` |
| path variable | `.uri("/users/{id}", 5)` | `/users/5` |
| query params | `.uri(u -> u.path("/users").queryParam("_page", 1).build())` | `/users?_page=1` |

**Pontos de atenção:**
- `.retrieve()` lança `HttpClientErrorException`/`HttpServerErrorException` em 4xx/5xx por padrão.
- `.onStatus(...)` fica **entre** o `.retrieve()` e o `.body()`; troca a exceção genérica por uma sua.
- API fora do ar não tem status: vira `ResourceAccessException` (tratar no handler ou com `try/catch`).

---

## 2 a 7. Próximos clientes

Para cada um: criar a classe em `clientes/`, implementar `listUsers(page, limit)` e `getUserById(id)`
e anotar aqui as diferenças pelos critérios acima.

| Cliente | Ponto-chave para estudar |
|---|---|
| `java.net.http.HttpClient` | `HttpRequest.newBuilder()`, `send` x `sendAsync`, conversão manual com `JsonMapper` |
| `HttpURLConnection` | abrir conexão, `getResponseCode()`, ler o `InputStream` na mão |
| `RestTemplate` | `getForObject`, `getForEntity`, `exchange`; por que virou legado |
| `WebClient` | `Mono`/`Flux`, `bodyToMono`, o que significa `.block()` |
| `@HttpExchange` | interface com `@GetExchange` + `HttpServiceProxyFactory` |
| Apache HttpClient 5 | `HttpGet`, `execute` com response handler, pool de conexões |

---

## Base comum do laboratório

Todos os clientes usam a mesma API, os mesmos modelos e o mesmo tratamento de erro.

### Endpoints expostos

| Método | Rota | Parâmetros | Resposta |
|---|---|---|---|
| `GET` | `/api/users` | `page` (padrão `1`), `limit` (padrão `5`) | `200` + lista de `UserDTO` |
| `GET` | `/api/users/{id}` | `id` (path) | `200` + `UserDTO` · `404` + `BodyAPIError` |

```http
GET http://localhost:8080/api/users?page=2&limit=3
GET http://localhost:8080/api/users/1
```

### `UserDTO`

| Campo | Tipo | Exemplo |
|---|---|---|
| `id` | `Long` | `1` |
| `name` | `String` | `"Leanne Graham"` |
| `username` | `String` | `"Bret"` |
| `email` | `String` | `"Sincere@april.biz"` |

Campos extras do JSON (`phone`, `address`, `company`...) são ignorados pelo Jackson.

### `BodyAPIError`

| Campo | Tipo | Exemplo |
|---|---|---|
| `code` | `int` | `404` |
| `errorName` | `String` | `"Usuário não encontrado"` |
| `message` | `String` | `"Usuário não encontrado: id=999"` |
| `uriLocation` | `String` | `"/api/users/999"` |
| `timestamp` | `Instant` | `"2026-11-23T17:30:00Z"` |

### Erros

| Exceção | Quando | Status |
|---|---|---|
| `UserNotFoundException` | API externa responde 404 no busca por id | `404` |
| `ExternalApiException` | API externa responde 5xx | `500` (ainda sem handler próprio) |
| `Exception` | qualquer outro erro | `500` |

Fluxo: o cliente HTTP detecta o status → lança a exceção → `Global` (`@RestControllerAdvice`) converte em `BodyAPIError`.

---

## Como rodar

```powershell
.\mvnw.cmd spring-boot:run
```

Ou rode `ReqApplication` pela IDE. Sobe em `http://localhost:8080`.

| Propriedade (`application.yaml`) | Valor |
|---|---|
| `api.base-url` | `https://jsonplaceholder.typicode.com` |

---

## Estrutura

```
src/main/java/estudo/requisicoes/req/
├── clientes/                  um arquivo por cliente HTTP (RestClientExemplo = @Bean do RestClient)
├── controller/restClient/     UserController
├── service/restClient/        UserService (usa o RestClient)
├── dtos/                      UserDTO, BodyAPIError
└── exception/                 UserNotFoundException, ExternalApiException, Global (handler)
```

---

## Próximos passos

- [ ] Implementar os clientes 2 a 7
- [ ] Handler de `ExternalApiException` respondendo `502 Bad Gateway`
- [ ] Handler de `ResourceAccessException` (API fora do ar / timeout)
- [ ] Ler `X-Total-Count` com `.toEntity(...)` e devolver os metadados da paginação

---

## Referências

- [Spring — REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Java — HttpClient](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html)
- [Apache HttpClient 5](https://hc.apache.org/httpcomponents-client-5.4.x/)
- [JSONPlaceholder — guia](https://jsonplaceholder.typicode.com/guide/)
