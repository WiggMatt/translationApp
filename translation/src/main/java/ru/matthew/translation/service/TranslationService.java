package ru.matthew.translation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.matthew.auth.TokenManager;
import ru.matthew.translation.repository.TranslationRequestRepository;
import ru.matthew.translation.model.TranslationRequest;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
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
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public String translateText(String sourceLanguageCode, String targetLanguageCode, String texts) {
        String[] words = texts.split("\\s+");
        List<Future<String>> futures = new ArrayList<>();

        for (String word : words) {
            futures.add(executorService.submit(() -> translateWord(sourceLanguageCode, targetLanguageCode, word)));
        }

        try {
            return futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.joining(" "));
        } catch (RuntimeException e) {
            throw new RuntimeException("Error during translation", e);
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

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, String>> translations = (List<Map<String, String>>) response.getBody().get("translations");
            return translations.get(0).get("text");
        } else {
            throw new RuntimeException("Error during translation");
        }
    }

    public void saveRequest(String ipAddress, String inputText, String translatedText) {
        TranslationRequest request = TranslationRequest.builder()
                .ipAddress(ipAddress)
                .inputText(inputText)
                .translatedText(translatedText)
                .build();
        translationRequestRepository.saveRequest(request);
    }
}
