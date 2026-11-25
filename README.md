<h1 align="center">Req — Clientes HTTP em Java e Spring</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4">
  <img src="https://img.shields.io/badge/Spring_Web-MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Web MVC">
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge" alt="Lombok">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/RestClient-✓-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="RestClient">
  <img src="https://img.shields.io/badge/java.net.http.HttpClient-✓-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java HttpClient">
  <img src="https://img.shields.io/badge/HttpURLConnection-✓-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="HttpURLConnection">
  <img src="https://img.shields.io/badge/RestTemplate-✓-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="RestTemplate">
  <img src="https://img.shields.io/badge/@HttpExchange-✓-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="HttpExchange">
  <img src="https://img.shields.io/badge/WebClient-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="WebClient">
  <img src="https://img.shields.io/badge/Apache_HttpClient_5-D22128?style=flat-square&logo=apache&logoColor=white" alt="Apache HttpClient 5">
</p>

<p align="center">
  Estudo das formas de <strong>fazer requisições HTTP</strong> em Java: a mesma chamada,
  implementada com cada cliente, para comparar lado a lado.
</p>

---

## Objetivo

Implementar **as mesmas requisições** com cada cliente HTTP e entender o que cada um faz (ou deixa para você fazer):

- `GET /users?_page=&_limit=` → lista paginada
- `GET /users/{id}` → um usuário (404 se não existir)
- `GET /users?email=` → busca por email (filtro devolve lista)

API alvo: [JSONPlaceholder](https://jsonplaceholder.typicode.com), pública e gratuita.

---

## Progresso

| # | Cliente | Origem | Dependência | Estilo | Status |
|---|---|---|---|---|---|
| 1 | **RestClient** | Spring 6.1+ | `spring-boot-starter-restclient` | síncrono, fluente | ✅ |
| 2 | **`java.net.http.HttpClient`** | Java 11+ | nenhuma | síncrono ou assíncrono | ✅ |
| 3 | **`HttpURLConnection`** | Java 1.1 | nenhuma | baixo nível, mutável | ✅ |
| 4 | **`RestTemplate`** | Spring (legado) | `spring-boot-starter-restclient` | um método pronto por caso | ✅ |
| 5 | **`@HttpExchange`** | Spring 6+ | `spring-boot-starter-restclient` | declarativo (só interface) | ✅ |
| 6 | `WebClient` | Spring WebFlux | `spring-boot-starter-webclient` | reativo (`Mono`/`Flux`) | ⬜ |
| 7 | Apache HttpClient 5 | terceiros | `httpclient5` | baixo nível, configurável | ⬜ |

---

## Comparativo dos clientes feitos

A mesma requisição passa sempre por **4 etapas**: montar, enviar, conferir o status e converter o JSON.
O que muda é **quem faz cada uma** — você ou o cliente.

| Etapa | RestClient | HttpClient | HttpURLConnection | RestTemplate | @HttpExchange |
|---|---|---|---|---|---|
| **Criar** | `@Bean` com Builder | campo `final` (`newBuilder()`) | **local**, 1 por requisição | `@Bean` com `RestTemplateBuilder` | `@Bean` que gera um **proxy** da interface |
| **Montar** | cadeia `.get().uri(...)` | `HttpRequest.newBuilder()` | `setRequestMethod` / `setRequestProperty` | argumentos do método (`getForObject(url, tipo, vars)`) | **anotações** na interface |
| **Query params** | `.queryParam(...)` | `UriComponentsBuilder` / `URLEncoder` | concatenação / `URLEncoder` | `{}` no template | `@RequestParam` |
| **Enviar** | `.retrieve()` | `client.send(...)` | implícito no `getResponseCode()` | a própria chamada | chamar o método da interface |
| **Status** | exceção ou `.onStatus` | `statusCode()`, nunca lança | `getResponseCode()` (`int`) | exceção, **sem `onStatus`** → `try/catch` | exceção → `try/catch` |
| **JSON** | automático | manual (`jsonMapper`) | manual (`jsonMapper`) | automático | automático (tipo de retorno) |
| **Lista** | `ParameterizedTypeReference` | `TypeReference` | `TypeReference` | `X[].class` ou `exchange(...)` | `List<X>` no retorno |
| **Linhas no service p/ um GET** | ~5 | ~10 | ~20 (no componente) | 1 | **1** |

**Conclusão:** HttpURLConnection e HttpClient mostram o que acontece de verdade; RestClient e RestTemplate fazem
o mesmo com o Spring cuidando do JSON e dos erros; o `@HttpExchange` vai além e gera o código a partir de uma interface —
mas, por baixo, é o RestClient executando.

---

## 1. RestClient ✅

`clientes/RestClientExemplo` (`@Configuration`) expõe um `@Bean RestClient` com `baseUrl` do yaml e `Accept` padrão.

```java
restClient.get()
        .uri("/users/{id}", id)
        .retrieve()
        .onStatus(s -> s.value() == 404, (req, res) -> {
            throw new UserNotFoundException(id);
        })
        .body(UserDTO.class);
```

**Aprendizados**
- `.onStatus` fica **entre** `.retrieve()` e `.body()`; troca a exceção genérica do Spring por uma de negócio.
- `.body(X.class)` para objeto; `ParameterizedTypeReference<List<X>>() {}` para lista; `.toEntity(...)` para status + headers.
- Filtro por query (`?email=`) devolve **lista** e responde **200 com `[]`** quando não acha.
- `@Component` registra a classe; `@Bean` registra **o que o método retorna**.

## 2. `java.net.http.HttpClient` ✅

`clientes/JavaHttpClientExemplo` guarda o `HttpClient` e expõe `get(path, Map<String, ?> queryParams)`
devolvendo `HttpResponse<String>`. O service checa o status e converte.

```java
HttpResponse<String> response = clientHttp.get("/users", Map.of("_page", page, "_limit", limit));
if (response.statusCode() >= 500) throw new ExternalApiException(...);
return jsonMapper.readValue(response.body(), new TypeReference<List<UserDTO>>() {});
```

**Aprendizados**
- `HttpClient` é abstrato: cria-se por fábrica (`newHttpClient()` / `newBuilder()`), não com `new`.
- Não precisa ser `@Bean`: não depende do Spring. A **classe que o usa** é que precisa ser bean.
- `send(request, BodyHandler)`: o 2º argumento diz **como ler** o corpo (`ofString`, `ofByteArray`...).
- `HttpRequest` é imutável: o cliente se reaproveita, a requisição é criada a cada chamada.
- `Map<String, ?>`: o coringa aceita valores de qualquer tipo.
- `TypeReference` certo é `tools.jackson.core.type.TypeReference`, não o do `org.springframework.aot.hint`.

## 3. `HttpURLConnection` ✅

`clientes/HttpUrlConnectionExemplo` abre uma conexão por chamada e devolve um `RespostaHttp(status, body)`.

```java
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
int status = connection.getResponseCode();               // a requisição sai AQUI
InputStream stream = (status >= 400) ? connection.getErrorStream() : connection.getInputStream();
```

**Aprendizados**
- A conexão é **local**, não campo: nasce de uma URL e serve para uma requisição só.
- Configuração só antes do `getResponseCode()`.
- Dois "canos": `getInputStream()` lança exceção em 4xx/5xx; o corpo do erro fica no `getErrorStream()` (pode ser `null`).
- Sem timeout configurado, o padrão é esperar para sempre.
- O `RespostaHttp` é o pacote que o `HttpResponse` do HttpClient já entregava pronto.

## 4. `RestTemplate` ✅

`clientes/RestTemplateExemplo` (`@Configuration`) cria o `@Bean RestTemplate` com `RestTemplateBuilder`
(`rootUri`, timeouts, headers). O service injeta o **`RestTemplate`**, não a classe de configuração.

```java
try {
    return restTemplate.getForObject("/users/{id}", UserDTO.class, id);
} catch (HttpClientErrorException.NotFound e) {
    throw new UserNotFoundException(id);
}
```

**Aprendizados**
- A requisição inteira cabe **numa chamada de método**: `getForObject`, `getForEntity` ou `exchange`.
- Configuração vai no **builder, antes do `build()`**; o `RestTemplate` pronto não tem métodos de configuração.
- `...ForObject` não aceita `List<X>`: usar `X[].class` ou `exchange(...)` com `ParameterizedTypeReference`.
- Não existe `onStatus`: o 404 vira `HttpClientErrorException.NotFound`, tratado com `try/catch`.
- Query params no próprio template (`"/users?email={email}"`), com encoding automático.
- Exceção não é valor: não cabe num ternário, precisa de `if` + `throw`.

## 5. `@HttpExchange` ✅

`clientes/HttpExchangeExemplo` é **só uma interface**. `clientes/HttpExchangeConfig` cria o bean dela:

```java
RestClient restClient = builder.baseUrl(baseUrl).build();          // o motor
RestClientAdapter adapter = RestClientAdapter.create(restClient);  // o encaixe
HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
return factory.createClient(HttpExchangeExemplo.class);            // gera a implementação
```

```java
@HttpExchange("/users")
public interface HttpExchangeExemplo {
    @GetExchange("/{id}") UserDTO getById(@PathVariable Long id);
    @GetExchange List<UserDTO> getListUsers(@RequestParam("_page") int page, @RequestParam("_limit") int limit);
    @GetExchange List<UserDTO> findByEmail(@RequestParam("email") String email);
}
```

**Aprendizados**
- A interface não tem corpo, então o `@Bean` fica numa `@Configuration` **separada**, que devolve o proxy.
- Por baixo **é o RestClient**: mesmo `baseUrl`, mesmo Jackson, mesmas exceções. O proxy traduz cada método numa cadeia do RestClient.
- O adapter permite trocar o motor (`RestClientAdapter`, `WebClientAdapter`, `RestTemplateAdapter`) sem mudar a interface.
- O tipo de retorno do método define o `.body(...)`: filtro por email continua sendo `List<UserDTO>`.
- O service fica só com regras (404 → exceção, lista vazia → exceção); a requisição é `api.getById(id)`.

---

## Endpoints

| Cliente | Rota base | `GET /` (lista) | `GET /{id}` | `GET /search?email=` |
|---|---|---|---|---|
| RestClient | `/api/restClient/users` | `page`, `limit` (padrão 1 e 5) | ✅ | ✅ |
| HttpClient | `/api/httpclient/users` | `page`, `limit` (obrigatórios) | — | — |
| HttpURLConnection | `/api/httpurl/users` | sem parâmetros | — | — |
| RestTemplate | `/api/template/users` | `page` `@Min(1)`, `limit` `@Min(1)` `@Max(100)` | ✅ | — |
| @HttpExchange | `/api/exchange/users` | `page` `@Min(1)`, `limit` `@Min(1)` `@Max(100)` | ✅ | ✅ |

```http
GET http://localhost:8080/api/restClient/users?page=2&limit=3
GET http://localhost:8080/api/template/users/1
GET http://localhost:8080/api/exchange/users/search?email=Shanna@melissa.tv
```

**Rotas:** `/{id}` e `/{email}` com o mesmo verbo têm o mesmo formato e geram "Ambiguous mapping" —
por isso a busca por email usa um caminho fixo (`/search`) com `@RequestParam`.

---

## Modelos

### `UserDTO`

| Campo | Tipo | Exemplo |
|---|---|---|
| `id` | `Long` | `1` |
| `name` | `String` | `"Leanne Graham"` |
| `username` | `String` | `"Bret"` |
| `email` | `String` | `"Sincere@april.biz"` |

### `BodyAPIError`

| Campo | Tipo | Exemplo |
|---|---|---|
| `code` | `int` | `404` |
| `errorName` | `String` | `"Usuário não encontrado"` |
| `message` | `String` | `"Usuário não encontrado: id=999"` |
| `uriLocation` | `String` | `"/api/template/users/999"` |
| `timestamp` | `Instant` | `"2026-11-25T17:06:54Z"` |

### `RespostaHttp`

| Campo | Tipo | Descrição |
|---|---|---|
| `status` | `int` | código HTTP lido com `getResponseCode()` |
| `body` | `String` | corpo lido do `InputStream` ou `ErrorStream` |

---

## Erros

Toda exceção que pode acontecer precisa de **um `@ExceptionHandler` no `Global`**. Sem handler próprio,
cai no genérico e vira `500` — mesmo quando o erro é de quem chamou.

| Exceção | Quando | Status |
|---|---|---|
| `UserNotFoundException` | API responde 404 na busca por id | `404` |
| `EmailNotFoundException` | filtro por email volta lista vazia | `404` |
| `SearchEmailException` | email em branco ou sem `@` | `400` |
| `PageNotFoundException` | página sem usuários | `404` |
| `MethodArgumentTypeMismatchException` | letra num parâmetro numérico (`?page=abc`) — o Spring barra antes do método | `400` |
| `HandlerMethodValidationException` | `@Min` / `@Max` falhou (`?limit=999`) | `400` |
| `ExternalApiException` | API responde 5xx ou não responde | `500` (sem handler próprio) |
| `Exception` | qualquer outro erro | `500` |

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
├── clientes/
│   ├── RestClientExemplo.java          @Bean RestClient
│   ├── JavaHttpClientExemplo.java      HttpClient + get(path, queryParams)
│   ├── HttpUrlConnectionExemplo.java   abre conexão e devolve RespostaHttp
│   ├── RestTemplateExemplo.java        @Bean RestTemplate (RestTemplateBuilder)
│   ├── HttpExchangeExemplo.java        interface declarativa
│   ├── HttpExchangeConfig.java         @Bean que gera o proxy da interface
│   └── ...Exemplo.java                 próximos clientes
├── controller/   restClient/ · httpClient/ · httpUrlClient/ · restTemplate/ · httpExchange/
├── service/      restClient/ · httpClient/ · httpUrlConnection/ · UserService/ · httpExchange/
├── dtos/         UserDTO, BodyAPIError, RespostaHttp
└── exception/    exceções customizadas + Global (@RestControllerAdvice)
```

Classes com o mesmo nome (ou o mesmo nome de um método `@Bean`) geram conflito de bean:
cada service/controller precisa de um nome único.

---

## Próximos passos

- [ ] WebClient
- [ ] Apache HttpClient 5
- [ ] Busca por id e email no HttpClient e no HttpURLConnection; email no RestTemplate
- [ ] Handler de `ExternalApiException` respondendo `502 Bad Gateway`
- [ ] Handler de `MissingServletRequestParameterException` (parâmetro obrigatório ausente → `400`)

---

## Referências

- [Spring — REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Spring — HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
- [Java — HttpClient](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html)
- [Java — HttpURLConnection](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/HttpURLConnection.html)
- [JSONPlaceholder — guia](https://jsonplaceholder.typicode.com/guide/)
