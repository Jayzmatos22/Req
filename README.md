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
  <img src="https://img.shields.io/badge/RestTemplate-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="RestTemplate">
  <img src="https://img.shields.io/badge/@HttpExchange-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="HttpExchange">
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
| 4 | `RestTemplate` | Spring (legado) | `spring-boot-starter-restclient` | síncrono, métodos prontos | ⬜ |
| 5 | `@HttpExchange` | Spring 6+ | `spring-boot-starter-restclient` | declarativo (só interface) | ⬜ |
| 6 | `WebClient` | Spring WebFlux | `spring-boot-starter-webclient` | reativo (`Mono`/`Flux`) | ⬜ |
| 7 | Apache HttpClient 5 | terceiros | `httpclient5` | baixo nível, configurável | ⬜ |

---

## Comparativo dos clientes feitos

A mesma requisição passa sempre por **4 etapas**. O que muda é quem faz cada uma:

| Etapa | RestClient | HttpClient | HttpURLConnection |
|---|---|---|---|
| **Criar o cliente** | `@Bean` com `baseUrl` (precisa do Builder do Spring) | campo `final`, `HttpClient.newBuilder()` — reaproveitado | **variável local**, uma conexão por requisição |
| **Montar** | `.get().uri(...)` | `HttpRequest.newBuilder()` (imutável) | `setRequestMethod`, `setRequestProperty` (mutável) |
| **Query params** | `.uri(u -> u.queryParam(...))`, encoding automático | não tem: `UriComponentsBuilder` ou `URLEncoder` | não tem: concatenação ou `URLEncoder` |
| **Enviar** | `.retrieve()` | `client.send(request, BodyHandlers.ofString())` | **implícito**: sai no `getResponseCode()` |
| **Status** | exceção automática em 4xx/5xx, ou `.onStatus(...)` | `response.statusCode()` — nunca lança | `getResponseCode()` — devolve `int` |
| **Corpo** | `.body(UserDTO.class)` já convertido | `response.body()` → `String` | `getInputStream()` / `getErrorStream()` → bytes → `String` |
| **JSON** | automático | manual: `jsonMapper.readValue(...)` | manual: `jsonMapper.readValue(...)` |
| **Exceções checked** | nenhuma | `IOException`, `InterruptedException` no `send` | `IOException` em quase tudo |
| **Fechar** | automático | automático | `disconnect()` no `finally` |
| **Assíncrono** | não | `sendAsync` → `CompletableFuture` | não |

**Conclusão até aqui:** o RestClient esconde as 4 etapas numa cadeia; o HttpClient expõe cada uma, mas com API moderna;
o HttpURLConnection expõe tudo e ainda obriga a lidar com streams, conexão mutável e dois "canos" de corpo.

---

## 1. RestClient ✅

`clientes/RestClientExemplo` (`@Configuration`) expõe um `@Bean RestClient` com `baseUrl` do yaml e `Accept` padrão.
O service recebe esse bean pronto.

```java
restClient.get()
        .uri("/users/{id}", id)                    // path variable
        .retrieve()                                // envia
        .onStatus(s -> s.value() == 404, (req, res) -> {   // reage ao status antes de ler o corpo
            throw new UserNotFoundException(id);
        })
        .body(UserDTO.class);                      // converte o JSON
```

**Aprendizados**
- `.onStatus` fica **entre** `.retrieve()` e `.body()`; troca a exceção genérica do Spring por uma de negócio.
- `.body(X.class)` para objeto; `ParameterizedTypeReference<List<X>>() {}` para lista; `.toEntity(...)` para status + headers.
- Filtro por query (`?email=`) devolve **lista** e responde **200 com `[]`** quando não acha — o 404 não vem, quem decide é você.
- `@Component` registra a classe; `@Bean` registra **o que o método retorna**.

## 2. `java.net.http.HttpClient` ✅

`clientes/JavaHttpClientExemplo` guarda o `HttpClient` (campo, criado uma vez), monta a URL com `UriComponentsBuilder`
e expõe `get(path, Map<String, ?> queryParams)` devolvendo `HttpResponse<String>`. O service checa o status e converte.

```java
HttpResponse<String> response = clientHttp.get("/users", Map.of("_page", page, "_limit", limit));
if (response.statusCode() >= 500) throw new ExternalApiException(...);
return jsonMapper.readValue(response.body(), new TypeReference<List<UserDTO>>() {});
```

**Aprendizados**
- `HttpClient` é abstrato: cria-se por fábrica (`newHttpClient()` / `newBuilder()`), não com `new`.
- Não precisa ser `@Bean`: não depende de nada do Spring. A **classe que o usa** é que precisa ser bean.
- `send(request, BodyHandler)`: o 2º argumento diz **como ler** o corpo (`ofString`, `ofByteArray`, `ofFile`...).
- `HttpRequest` é imutável: o cliente se reaproveita, a requisição é criada a cada chamada.
- `Map<String, ?>`: o coringa aceita mapas com valores de qualquer tipo (texto e números).
- `TypeReference` certo é `tools.jackson.core.type.TypeReference` (Jackson 3), não o do `org.springframework.aot.hint`.

## 3. `HttpURLConnection` ✅

`clientes/HttpUrlConnectionExemplo` abre uma conexão por chamada e devolve um `RespostaHttp(status, body)`
— o "pacote" que o `HttpResponse` do HttpClient já entregava pronto.

```java
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Accept", "application/json");
int status = connection.getResponseCode();               // a requisição sai AQUI
InputStream stream = (status >= 400) ? connection.getErrorStream() : connection.getInputStream();
```

**Aprendizados**
- A conexão é **local**, não campo: nasce de uma URL e serve para uma requisição só.
- Configuração só antes do `getResponseCode()`; depois disso dá `IllegalStateException`.
- Dois "canos": `getInputStream()` lança exceção em 4xx/5xx; o corpo do erro fica no `getErrorStream()` (pode ser `null`).
- Sem timeout configurado, o padrão é esperar para sempre.
- Bytes → texto: `new String(stream.readAllBytes(), StandardCharsets.UTF_8)`.
- O componente não decide nada sobre status; o service interpreta o `RespostaHttp`.

---

## Endpoints

| Cliente | Método | Rota | Parâmetros |
|---|---|---|---|
| RestClient | `GET` | `/api/restClient/users` | `page` (padrão 1), `limit` (padrão 5) |
| RestClient | `GET` | `/api/restClient/users/{id}` | `id` |
| RestClient | `GET` | `/api/restClient/users/search` | `email` |
| HttpClient | `GET` | `/api/httpclient/users` | `page`, `limit` (obrigatórios) |
| HttpURLConnection | `GET` | `/api/httpurl/users` | — |

```http
GET http://localhost:8080/api/restClient/users?page=2&limit=3
GET http://localhost:8080/api/restClient/users/1
GET http://localhost:8080/api/restClient/users/search?email=Shanna@melissa.tv
GET http://localhost:8080/api/httpclient/users?page=1&limit=5
GET http://localhost:8080/api/httpurl/users
```

---

## Modelos

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
| `uriLocation` | `String` | `"/api/restClient/users/999"` |
| `timestamp` | `Instant` | `"2026-11-24T16:22:39Z"` |

### `RespostaHttp`

| Campo | Tipo | Descrição |
|---|---|---|
| `status` | `int` | código HTTP lido com `getResponseCode()` |
| `body` | `String` | corpo lido do `InputStream` ou `ErrorStream` |

---

## Erros

Toda exceção customizada precisa de **dois passos**: a classe e o `@ExceptionHandler` no `Global`.
Sem handler próprio, cai no genérico e vira `500`.

| Exceção | Quando | Status |
|---|---|---|
| `UserNotFoundException` | API responde 404 na busca por id | `404` |
| `EmailNotFoundException` | filtro por email volta lista vazia | `404` |
| `SearchEmailException` | email em branco ou sem `@` | `400` |
| `PageNotFoundException` | página sem usuários (HttpClient) | `404` |
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
│   └── ...Exemplo.java                 próximos clientes
├── controller/
│   ├── restClient/UserController
│   ├── httpClient/UserControllerHttp
│   └── httpUrlClient/UserControllerUrl
├── service/
│   ├── restClient/UserServiceHttp
│   ├── httpClient/UserService
│   └── httpUrlConnection/UserServiceHttpUrl
├── dtos/          UserDTO, BodyAPIError, RespostaHttp
└── exception/     exceções customizadas + Global (@RestControllerAdvice)
```

Classes com o mesmo nome em pacotes diferentes geram `ConflictingBeanDefinitionException`:
cada service/controller precisa de um nome único.

---

## Próximos passos

- [ ] RestTemplate
- [ ] `@HttpExchange`
- [ ] WebClient
- [ ] Apache HttpClient 5
- [ ] Busca por id e email no HttpClient e no HttpURLConnection
- [ ] Handler de `ExternalApiException` respondendo `502 Bad Gateway`

---

## Referências

- [Spring — REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Java — HttpClient](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html)
- [Java — HttpURLConnection](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/HttpURLConnection.html)
- [JSONPlaceholder — guia](https://jsonplaceholder.typicode.com/guide/)
