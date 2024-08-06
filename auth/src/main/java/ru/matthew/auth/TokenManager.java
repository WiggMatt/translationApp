package ru.matthew.auth;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TokenManager {

    private static final Logger logger = Logger.getLogger(TokenManager.class.getName());
    private final TokenFetcher tokenFetcher;
    @Getter
    private String iamToken;

    @Autowired
    public TokenManager(TokenFetcher tokenFetcher) {
        this.tokenFetcher = tokenFetcher;
        refreshToken();
    }

    @Scheduled(fixedRate = 3600000)
    public void refreshToken() {
        try {
            this.iamToken = tokenFetcher.fetchToken();
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении токена. Перезапуск серверва чере 1 минуту");
            retryRefreshToken();
        }
    }

    private void retryRefreshToken() {
        try {
            Thread.sleep(60000);
            this.iamToken = tokenFetcher.fetchToken();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Поток был прерван во время ожидания перед повторной попыткой обновления токена", ie);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Повторная ошибка при обновлении токена. Перезапуск серверва чере 1 минуту");
        }
    }
}
