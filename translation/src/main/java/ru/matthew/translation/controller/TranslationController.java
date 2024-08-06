package ru.matthew.translation.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.matthew.translation.exception.DatabaseException;
import ru.matthew.translation.exception.ThreadException;
import ru.matthew.translation.exception.YandexAPIAccessException;
import ru.matthew.translation.model.TranslationRequestDTO;
import ru.matthew.translation.service.TranslationService;

@RestController
@RequestMapping()
public class TranslationController {
    private final TranslationService translationService;

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping("/languages")
    public ResponseEntity<String> getLanguages() {
        try {
            String languages = translationService.getLanguages();
            return ResponseEntity.ok(languages);
        } catch (YandexAPIAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestBody TranslationRequestDTO translationRequestDTO,
                                            HttpServletRequest request) {
        if (isNullOrEmpty(translationRequestDTO.getSourceLanguageCode()) ||
                isNullOrEmpty(translationRequestDTO.getTargetLanguageCode()) ||
                isNullOrEmpty(translationRequestDTO.getTexts())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Некорректные входные данные: не указаны все необходимые параметры.");
        }

        try {
            String translatedText = translationService.translateText(
                    translationRequestDTO.getSourceLanguageCode(),
                    translationRequestDTO.getTargetLanguageCode(),
                    translationRequestDTO.getTexts()
            );

            String ipAddress = request.getRemoteAddr();
            translationService.saveRequest(ipAddress, translationRequestDTO.getTexts(), translatedText);

            return ResponseEntity.ok(translatedText);

        } catch (ThreadException | DatabaseException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        } catch (YandexAPIAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
