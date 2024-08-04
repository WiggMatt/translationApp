package ru.matthew.translationApp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.matthew.translationApp.repository.TranslationRequestRepository;
import ru.matthew.translationApp.service.TranslationService;

@RestController
public class TranslationController {

    private final TranslationService translationService;
    private final TranslationRequestRepository translationRequestRepository;

    public TranslationController(TranslationService translationService, TranslationRequestRepository translationRequestRepository) {
        this.translationService = translationService;
        this.translationRequestRepository = translationRequestRepository;
    }

    @GetMapping("/languages")
    public String getLanguages(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        translationRequestRepository.saveRequest(ipAddress);
        return translationService.getLanguages("b1gloo1se7vi30bsl33b");
    }
}
