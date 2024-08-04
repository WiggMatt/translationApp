package ru.matthew.translationApp.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {
    public String getLanguages(String folderId) {
        String url = "https://translate.api.cloud.yandex.net/translate/v2/languages";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer ");

        Map<String, String> body = new HashMap<>();
        body.put("folderId", folderId);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }
}
