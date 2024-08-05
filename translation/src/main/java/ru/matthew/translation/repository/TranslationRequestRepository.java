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
            // Обработка ошибки, когда запись с таким же ключом уже существует
            throw new RuntimeException("Duplicate record error: " + e.getMessage(), e);
        } catch (DataAccessException e) {
            // Обработка общих ошибок доступа к данным
            throw new RuntimeException("Data access error: " + e.getMessage(), e);
        } catch (Exception e) {
            // Обработка других неожиданных ошибок
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }
}