package ru.matthew.auth;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenManager {

    private final TokenFetcher tokenFetcher;
    @Getter
    private String iamToken;

    @Autowired
    public TokenManager(TokenFetcher tokenFetcher) {
        this.tokenFetcher = tokenFetcher;
        refreshToken(); // Обновляем токен при старте
    }

    @Scheduled(fixedRate = 43200000) // 12 часов в миллисекундах
    public void refreshToken() {
        this.iamToken = tokenFetcher.fetchToken();
    }
}
