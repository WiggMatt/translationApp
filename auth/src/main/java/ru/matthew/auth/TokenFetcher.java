package ru.matthew.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class TokenFetcher {

    @Value("${yandex.oauth.token}")
    private String yandexOauthToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TokenFetcher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String fetchToken() {
        String url = "https://iam.api.cloud.yandex.net/iam/v1/tokens";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJson = String.format("{\"yandexPassportOauthToken\":\"%s\"}", yandexOauthToken);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        String responseBody = response.getBody();
        return parseTokenFromResponse(responseBody);
    }


    private String parseTokenFromResponse(String responseBody) {
        try {
            TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
            if (tokenResponse.getIamToken() == null) {
                throw new RuntimeException("В ответе отсутствует IAM токен");
            }
            return tokenResponse.getIamToken();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось распарсить токен из ответа: неверный формат JSON", e);
        }
    }
}
