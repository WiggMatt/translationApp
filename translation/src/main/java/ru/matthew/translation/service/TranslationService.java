package ru.matthew.translation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.matthew.auth.TokenManager;
import ru.matthew.translation.exception.DatabaseException;
import ru.matthew.translation.exception.ThreadException;
import ru.matthew.translation.exception.YandexAPIAccessException;
import ru.matthew.translation.model.TranslationRequest;
import ru.matthew.translation.repository.TranslationRequestRepository;

import java.util.*;
import java.util.concurrent.*;

@Service
public class TranslationService {
    private final TokenManager tokenManager;
    private final RestTemplate restTemplate;
    private final TranslationRequestRepository translationRequestRepository;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    private final Set<String> supportedLanguages = new HashSet<>();

    @Value("${folder.id}")
    private String folderId;

    @Autowired
    public TranslationService(TokenManager tokenManager, RestTemplate restTemplate,
                              TranslationRequestRepository translationRequestRepository) {
        this.tokenManager = tokenManager;
        this.restTemplate = restTemplate;
        this.translationRequestRepository = translationRequestRepository;
        this.executorService = Executors.newFixedThreadPool(8);
        this.objectMapper = new ObjectMapper();
    }

    private void loadSupportedLanguages() {
        try {
            String languagesJson = getLanguages();
            Map<String, Object> languagesMap = objectMapper.readValue(languagesJson, new TypeReference<>() {
            });
            List<Map<String, String>> languages = (List<Map<String, String>>) languagesMap.get("languages");
            for (Map<String, String> language : languages) {
                supportedLanguages.add(language.get("code"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке списка поддерживаемых языков", e);
        }
    }


    public String getLanguages() throws YandexAPIAccessException {
        String url = "https://translate.api.cloud.yandex.net/translate/v2/languages";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenManager.getIamToken());

        Map<String, String> body = new HashMap<>();
        body.put("folderId", folderId);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        return executeWithExceptionHandling(() -> {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Ошибка получения языков. HTTP статус: " + response.getStatusCode());
            }
        });
    }

    public String translateText(String sourceLanguageCode, String targetLanguageCode, String texts)
            throws ThreadException, YandexAPIAccessException {
        if (supportedLanguages.isEmpty()) {
            loadSupportedLanguages();
        }
        if (!supportedLanguages.contains(sourceLanguageCode) || !supportedLanguages.contains(targetLanguageCode)) {
            throw new IllegalArgumentException("Один или оба указанных языка не поддерживаются.");
        }

        String[] words = texts.split("\\s+");
        List<Future<String>> futures = new ArrayList<>();

        try {
            for (String word : words) {
                futures.add(executorService.submit(() -> translateWord(sourceLanguageCode, targetLanguageCode, word)));
            }
            StringBuilder translatedText = new StringBuilder();
            for (Future<String> future : futures) {
                translatedText.append(future.get()).append(" ");
            }
            return translatedText.toString().trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ThreadException("Поток был прерван во время перевода");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof YandexAPIAccessException) {
                throw (YandexAPIAccessException) cause;
            } else {
                throw new ThreadException("Ошибка выполнения задач перевода");
            }
        }
    }

    private String translateWord(String sourceLanguageCode, String targetLanguageCode, String word)
            throws YandexAPIAccessException {
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

        return executeWithExceptionHandling(() -> {
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
                throw new RuntimeException("Ошибка API перевода. HTTP статус: " + response.getStatusCode());
            }
        });
    }

    private <T> T executeWithExceptionHandling(Callable<T> action) throws YandexAPIAccessException {
        try {
            return action.call();
        } catch (ResourceAccessException e) {
            throw new YandexAPIAccessException("Ошибка подключения к сети");
        } catch (RestClientException e) {
            throw new YandexAPIAccessException("Ошибка при запросе API перевода");
        } catch (ClassCastException | NullPointerException e) {
            throw new YandexAPIAccessException("Ошибка при разборе ответа перевода");
        } catch (Exception e) {
            throw new YandexAPIAccessException("Ошибка при выполнении операции");
        }
    }

    public void saveRequest(String ipAddress, String inputText, String translatedText)
            throws DatabaseException {
        TranslationRequest request = TranslationRequest.builder()
                .ipAddress(ipAddress)
                .inputText(inputText)
                .translatedText(translatedText)
                .build();
        try {
            translationRequestRepository.saveRequest(request);
        } catch (DataAccessException e) {
            throw new DatabaseException("Ошибка сохранения запроса перевода в базу данных");
        }
    }
}
