package ru.matthew.translation.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.matthew.translation.service.TranslationService;
import ru.matthew.translation.model.TranslationRequestDTO;

@RestController
@RequestMapping
public class TranslationController {
    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }


    @GetMapping("/languages")
    public ResponseEntity<String> getLanguages() {
        try {
            String languages = translationService.getLanguages();
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get languages: " + e.getMessage());
        }
    }

    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestBody TranslationRequestDTO translationRequestDTO,
                                            HttpServletRequest request) {
        if (translationRequestDTO.getSourceLanguageCode() == null ||
                translationRequestDTO.getTargetLanguageCode() == null ||
                translationRequestDTO.getTexts() == null || translationRequestDTO.getTexts().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input data");
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status
                    (HttpStatus.BAD_REQUEST).body("Invalid translation request: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status
                    (HttpStatus.INTERNAL_SERVER_ERROR).body("Translation failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status
                    (HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred: " + e.getMessage());
        }
    }
}
