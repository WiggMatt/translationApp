package ru.matthew.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

        if (response.getStatusCode() == HttpStatus.OK) {
            return parseTokenFromResponse(response.getBody());
        } else {
            throw new RuntimeException("Failed to fetch IAM token");
        }
    }

    private String parseTokenFromResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new RuntimeException("Response body is empty");
        }

        try {
            TokenResponse tokenResponse = objectMapper.readValue(responseBody, TokenResponse.class);
            if (tokenResponse.getIamToken() == null) {
                throw new RuntimeException("IAM token is missing in the response");
            }
            return tokenResponse.getIamToken();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token from response", e);
        }
    }
}
