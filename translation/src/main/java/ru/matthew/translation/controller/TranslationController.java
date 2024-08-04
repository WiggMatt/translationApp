package ru.matthew.translation.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.matthew.translation.service.TranslationService;

@RestController
@RequestMapping("/translate")
public class TranslationController {
    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public ResponseEntity<String> translate(@RequestParam("sourceLanguageCode") String sourceLanguageCode,
                                            @RequestParam("targetLanguageCode") String targetLanguageCode,
                                            @RequestParam("texts") String texts,
                                            HttpServletRequest request) {
        try {
            String translatedText = translationService.translateText(sourceLanguageCode, targetLanguageCode, texts);
            String ipAddress = request.getRemoteAddr();
            translationService.saveRequest(ipAddress, texts, translatedText);
            return ResponseEntity.ok(translatedText);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Translation failed: " + e.getMessage());
        }
    }
}
