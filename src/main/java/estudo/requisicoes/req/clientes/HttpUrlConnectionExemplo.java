package estudo.requisicoes.req.clientes;

import estudo.requisicoes.req.dtos.RespostaHttp;
import estudo.requisicoes.req.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class HttpUrlConnectionExemplo {

    private final String baseUrl;

    public HttpUrlConnectionExemplo(@Value("${api.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RespostaHttp get(String path) {
        // variável local
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(baseUrl + path).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int status = connection.getResponseCode();

            InputStream stream = (status >= 400)
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            String body;
            // ler e converte
            try (stream) {
                body = (stream == null) ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }

            return new RespostaHttp(status, body);

        } catch (IOException e) {
            throw new ExternalApiException("Falha ao chamar a API: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
