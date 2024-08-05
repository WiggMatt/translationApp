package ru.matthew.translation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.matthew.auth.TokenManager;
import ru.matthew.translation.repository.TranslationRequestRepository;
import ru.matthew.translation.model.TranslationRequest;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.*;

@Service
public class TranslationService {
    private final TokenManager tokenManager;
    private final RestTemplate restTemplate;
    private final TranslationRequestRepository translationRequestRepository;
    private final ExecutorService executorService;
    @Value("${folder.id}")
    private String folderId;

    @Autowired
    public TranslationService(TokenManager tokenManager, RestTemplate restTemplate,
                              TranslationRequestRepository translationRequestRepository) {
        this.tokenManager = tokenManager;
        this.restTemplate = restTemplate;
        this.translationRequestRepository = translationRequestRepository;
        this.executorService = Executors.newFixedThreadPool(8);
    }

    public String getLanguages() {
        String url = "https://translate.api.cloud.yandex.net/translate/v2/languages";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenManager.getIamToken());

        Map<String, String> body = new HashMap<>();
        body.put("folderId", folderId);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get languages. HTTP status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error during API request for languages", e);
        }
    }

    public String translateText(String sourceLanguageCode, String targetLanguageCode, String texts) {
        String[] words = texts.split("\\s+");
        List<Future<String>> futures = new ArrayList<>();

        for (String word : words) {
            futures.add(executorService.submit(() -> translateWord(sourceLanguageCode, targetLanguageCode, word)));
        }

        try {
            StringBuilder translatedText = new StringBuilder();
            for (Future<String> future : futures) {
                translatedText.append(future.get()).append(" ");
            }
            return translatedText.toString().trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted during translation", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error during execution of translation tasks", e);
        }
    }

    private String translateWord(String sourceLanguageCode, String targetLanguageCode, String word) {
        String url = "https://translate.api.cloud.yandex.net/translate/v2/translate";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenManager.getIamToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("folderId", folderId);
        requestBody.put("sourceLanguageCode", sourceLanguageCode);
        requestBody.put("targetLanguageCode", targetLanguageCode);
        requestBody.put("texts", Collections.singletonList(word));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, String>> translations = (List<Map<String, String>>) body.get("translations");
                return translations.get(0).get("text");
            } else {
                throw new RuntimeException("Translation API error. HTTP status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error during API request for translation", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("Error parsing translation response", e);
        }
    }

    public void saveRequest(String ipAddress, String inputText, String translatedText) {
        TranslationRequest request = TranslationRequest.builder()
                .ipAddress(ipAddress)
                .inputText(inputText)
                .translatedText(translatedText)
                .build();
        try {
            translationRequestRepository.saveRequest(request);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error saving translation request to database", e);
        }
    }
}
