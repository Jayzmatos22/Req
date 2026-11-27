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
  <img src="https://img.shields.io/badge/WebClient-✓-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="WebClient">
  <img src="https://img.shields.io/badge/Apache_HttpClient_5-✓-D22128?style=flat-square&logo=apache&logoColor=white" alt="Apache HttpClient 5">
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
| 6 | **`WebClient`** | Spring WebFlux | `spring-boot-starter-webclient` | reativo (`Mono`/`Flux`) | ✅ |
| 7 | **Apache HttpClient 5** | terceiros | `httpclient5` | baixo nível, configurável | ✅ |

---

## Comparativo dos clientes feitos

A mesma requisição passa sempre por **4 etapas**: montar, enviar, conferir o status e converter o JSON.
O que muda é **quem faz cada uma** — você ou o cliente.

### Clientes do Spring

| Etapa | RestClient | RestTemplate | @HttpExchange | WebClient |
|---|---|---|---|---|
| **Criar** | `@Bean` com Builder | `@Bean` com `RestTemplateBuilder` | `@Bean` que gera um **proxy** da interface | `@Bean` com Builder + motor Reactor Netty |
| **Montar** | cadeia `.get().uri(...)` | argumentos do método (`getForObject(url, tipo, vars)`) | **anotações** na interface | cadeia `.get().uri(...)` (igual ao RestClient) |
| **Query params** | `.queryParam(...)` | `{}` no template | `@RequestParam` | `.queryParam(...)` |
| **Enviar** | `.retrieve()` | a própria chamada | chamar o método da interface | **quando alguém assina** (`.block()` ou o Spring) |
| **Status** | exceção ou `.onStatus` (lança) | exceção → `try/catch` | exceção → `try/catch` | `.onStatus` que **devolve** `Mono.error(...)` |
| **Resultado** | o objeto | o objeto | o objeto | `Mono<X>` / `Flux<X>` (promessa) |
| **Lista** | `ParameterizedTypeReference` | `X[].class` ou `exchange(...)` | `List<X>` no retorno | `bodyToFlux(X.class).collectList()` |
| **Timeout** | no builder | no builder (`connectTimeout`, `readTimeout`) | o do RestClient usado | no **motor** (`HttpClient` do Reactor Netty) |

### Clientes "manuais" (você faz cada etapa)

| Etapa | HttpClient (Java) | HttpURLConnection | Apache HttpClient 5 |
|---|---|---|---|
| **Criar** | campo `final` (`newBuilder()`) | **local**, 1 por requisição | `@Bean CloseableHttpClient` (`HttpClients.custom()`) |
| **Montar** | `HttpRequest.newBuilder()...GET()` | `setRequestMethod` / `setRequestProperty` | **uma classe por verbo**: `new HttpGet(uri)` |
| **Query params** | `UriComponentsBuilder` / `URLEncoder` | concatenação / `URLEncoder` | **`URIBuilder`** próprio |
| **Enviar** | `client.send(request, BodyHandlers.ofString())` | implícito no `getResponseCode()` | `execute(request, response -> {...})` |
| **Status** | `statusCode()` | `getResponseCode()` | `getCode()` |
| **Corpo** | `response.body()` | `getInputStream()` / `getErrorStream()` → bytes | `EntityUtils.toString(response.getEntity())` |
| **Fechar** | automático | `disconnect()` no `finally` | a resposta fecha sozinha; o client, no fim da aplicação |
| **Timeout** | `connectTimeout` no builder | `setConnectTimeout` / `setReadTimeout` | `RequestConfig` com `Timeout` |

Nos três: 4xx/5xx **não lançam exceção** (você confere o código) e o JSON é convertido **na mão** com `jsonMapper.readValue(...)`.

**Conclusão:** os clientes manuais mostram o que acontece de verdade — montar, enviar, conferir, converter, fechar.
Os do Spring fazem as mesmas etapas por você, cada um com um estilo: cadeia (RestClient), método pronto (RestTemplate),
interface (`@HttpExchange`) e reativo (WebClient). Por baixo, `@HttpExchange` é um RestClient, e RestClient/WebClient
podem usar Reactor Netty, JDK ou Apache como **motor**.

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

## 6. `WebClient` ✅

`clientes/WebClientExemplo` (`@Configuration`) cria o `@Bean WebClient` com o `WebClient.Builder` injetado e o
**Reactor Netty** como motor, onde ficam os timeouts:

```java
HttpClient httpClient = HttpClient.create()                       // reactor.netty.http.client.HttpClient
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
        .responseTimeout(Duration.ofSeconds(5));
return builder.baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
```

Duas rotas para a mesma busca — uma bloqueando, outra reativa:

```java
public UserDTO getById(Long id) {                 // GET /api/webclient/users/{id}
    return webClient.get().uri("/users/{id}", id)
            .retrieve()
            .onStatus(s -> s.value() == 404, response -> Mono.error(new UserNotFoundException(id)))
            .bodyToMono(UserDTO.class)
            .block();                             // espera aqui
}

public Mono<UserDTO> getByIdMono(Long id) {       // GET /api/webclient/users/reactive/{id}
    return webClient.get().uri("/users/{id}", id)
            .retrieve()
            .onStatus(s -> s.value() == 404, response -> Mono.error(new UserNotFoundException(id)))
            .bodyToMono(UserDTO.class);           // devolve a promessa
}
```

**O que os logs de thread mostraram**

```
[BLOCK] 1. entrou no service  -> http-nio-8080-exec-4
[BLOCK] 2. resposta chegou    -> reactor-http-nio-2
[BLOCK] 3. saiu do .block()   -> http-nio-8080-exec-4     ← a thread do Tomcat esperou até aqui

[MONO]  1. entrou no service  -> http-nio-8080-exec-8
[MONO]  2. devolveu o Mono    -> http-nio-8080-exec-8     ← a thread do Tomcat foi liberada aqui
[MONO]  3. resposta chegou    -> reactor-http-nio-2
```

No Postman as duas rotas respondem igual. A diferença é interna: no `.block()` a thread do Tomcat fica presa esperando
a API externa; no `Mono` ela é liberada e quem termina o trabalho é o Reactor. Sob carga (ou com `server.tomcat.threads.max: 1`
e `.delayElement(...)`), o `.block()` faz as requisições esperarem umas às outras.

**Aprendizados**
- `Mono<T>` = 0 ou 1 valor que **vai chegar**; `Flux<T>` = 0 a N valores. Nada é enviado até alguém **assinar**.
- O `onStatus` recebe **um** parâmetro (a resposta) e **devolve** `Mono.error(...)` — no reativo, erro é um valor do fluxo.
- `.block()` é a saída do mundo reativo para o normal: útil para estudar, mas anula a vantagem do WebClient.
- `Mono<ResponseEntity<X>>` (status decidido quando o valor chega) é o padrão; `ResponseEntity<Mono<X>>` fixa o status antes.
- Timeout fica no **motor** (Reactor Netty), não no builder do WebClient. Ou por requisição, com `.timeout(Duration)`.
- Cuidado com o import: o `HttpClient` do motor é `reactor.netty.http.client.HttpClient`, não o `java.net.http.HttpClient`.
- O aviso `java.lang.System::loadLibrary` no terminal vem do Netty no Java 25; some com `--enable-native-access=ALL-UNNAMED`.

## 7. Apache HttpClient 5 ✅

`clientes/ApacheHttpClientExemplo` (`@Configuration`) cria o `@Bean CloseableHttpClient` com **dois builders**:

```java
RequestConfig config = RequestConfig.custom()                     // 1. timeouts
        .setConnectionRequestTimeout(Timeout.ofSeconds(5))
        .setResponseTimeout(Timeout.ofSeconds(5))
        .build();

return HttpClients.custom()                                       // 2. o client
        .setDefaultRequestConfig(config)
        .setDefaultHeaders(List.of(new BasicHeader("Accept", "application/json")))
        .build();
```

No service, a resposta é tratada **dentro** de uma lambda:

```java
String body = httpClient.execute(new HttpGet(baseUrl + "/users/" + id), response -> {
    if (response.getCode() == 404) throw new UserNotFoundException(id);
    return EntityUtils.toString(response.getEntity());   // o que a lambda retorna, o execute retorna
});
return jsonMapper.readValue(body, UserDTO.class);
```

**Aprendizados**
- Biblioteca de terceiros, no mesmo nível do HttpClient do Java: 4 etapas manuais com outros nomes.
- **Uma classe por verbo**: `HttpGet`, `HttpPost`...
- O **response handler** fecha a resposta sozinho: fora da lambda ela já está fechada, então o corpo é lido lá dentro.
- Tem builder de query próprio (`URIBuilder`), que o HttpClient do Java não tem.
- Usa a própria classe de tempo (`Timeout`), não `Duration`; headers padrão são uma `List<Header>`.
- `RequestConfig` (timeouts) e `CloseableHttpClient` (client) são objetos diferentes, cada um com seu `.build()`.
- O Spring chama `close()` do client sozinho quando a aplicação para.

---

## Endpoints

| Cliente | Rota base | `GET /` (lista) | `GET /{id}` | `GET /search?email=` |
|---|---|---|---|---|
| RestClient | `/api/restClient/users` | `page`, `limit` (padrão 1 e 5) | ✅ | ✅ |
| HttpClient | `/api/httpclient/users` | `page`, `limit` (obrigatórios) | — | — |
| HttpURLConnection | `/api/httpurl/users` | sem parâmetros | — | — |
| RestTemplate | `/api/template/users` | `page` `@Min(1)`, `limit` `@Min(1)` `@Max(100)` | ✅ | — |
| @HttpExchange | `/api/exchange/users` | `page` `@Min(1)`, `limit` `@Min(1)` `@Max(100)` | ✅ | ✅ |
| WebClient | `/api/webclient/users` | — | ✅ (`.block()`) · `/reactive/{id}` (`Mono`) | — |
| Apache HttpClient 5 | `/api/apache/users` | — | ✅ | — |

```http
GET http://localhost:8080/api/restClient/users?page=2&limit=3
GET http://localhost:8080/api/template/users/1
GET http://localhost:8080/api/exchange/users/search?email=Shanna@melissa.tv
GET http://localhost:8080/api/webclient/users/reactive/1
GET http://localhost:8080/api/apache/users/1
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
│   ├── WebClientExemplo.java           @Bean WebClient (motor Reactor Netty)
│   └── ApacheHttpClientExemplo.java    @Bean CloseableHttpClient (RequestConfig)
├── controller/   restClient/ · httpClient/ · httpUrlClient/ · restTemplate/ · httpExchange/ · webClient/ · apacheHttp/
├── service/      restClient/ · httpClient/ · httpUrlConnection/ · UserService/ · httpExchange/ · webClient/ · apacheHttpClient5/
├── dtos/         UserDTO, BodyAPIError, RespostaHttp
└── exception/    exceções customizadas + Global (@RestControllerAdvice)
```

Classes com o mesmo nome (ou o mesmo nome de um método `@Bean`) geram conflito de bean:
cada service/controller precisa de um nome único.

---

## Próximos passos

- [x] Os 7 clientes
- [ ] Completar as 3 buscas (lista, id, email) em todos os clientes
- [ ] Apache HttpClient como **motor** do RestClient (`HttpComponentsClientHttpRequestFactory`)
- [ ] Remover os logs de thread do `UserServiceWebClient` (ou trocar por `@Slf4j`)
- [ ] Handler de `ExternalApiException` respondendo `502 Bad Gateway`
- [ ] Handler de `MissingServletRequestParameterException` (parâmetro obrigatório ausente → `400`)

---

## Referências

- [Spring — REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Spring — HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
- [Java — HttpClient](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html)
- [Java — HttpURLConnection](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/HttpURLConnection.html)
- [Spring — WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Project Reactor — Mono e Flux](https://projectreactor.io/docs/core/release/reference/)
- [Apache HttpClient 5](https://hc.apache.org/httpcomponents-client-5.4.x/)
- [JSONPlaceholder — guia](https://jsonplaceholder.typicode.com/guide/)
