package ru.matthew.translation.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.matthew.translation.model.TranslationRequest;

@Repository
public class TranslationRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    public TranslationRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveRequest(TranslationRequest request) {
        String sql = "INSERT INTO TRANSLATION_REQUESTS (ip_address, input_text, translated_text) VALUES (?, ?, ?)";

        try {
            jdbcTemplate.update(sql, request.getIpAddress(), request.getInputText(), request.getTranslatedText());
        } catch (DuplicateKeyException e) {
            // Обработка ошибки дублирования ключа
            throw new RuntimeException("Ошибка сохранения запроса перевода: дублирование ключа. Возможно, такой запрос уже существует.", e);
        } catch (DataAccessException e) {
            // Общие ошибки доступа к данным
            throw new RuntimeException("Ошибка доступа к базе данных при сохранении запроса перевода.", e);
        } catch (Exception e) {
            // Обработка всех остальных исключений
            throw new RuntimeException("Неизвестная ошибка при сохранении запроса перевода.", e);
        }
    }
}
