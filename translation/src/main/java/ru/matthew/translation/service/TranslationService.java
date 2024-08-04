package ru.matthew.translation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.matthew.auth.TokenManager;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {
    private final TokenManager tokenManager;
    private final RestTemplate restTemplate;

    @Autowired
    public TranslationService(TokenManager tokenManager, RestTemplate restTemplate) {
        this.tokenManager = tokenManager;
        this.restTemplate = restTemplate;
    }

    public String getLanguages(String folderId) {
        String url = "https://translate.api.cloud.yandex.net/translate/v2/languages";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenManager.getIamToken());

        Map<String, String> body = new HashMap<>();
        body.put("folderId", folderId);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }
}
