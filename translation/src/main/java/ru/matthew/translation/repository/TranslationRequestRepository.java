package ru.matthew.translation.repository;

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
        jdbcTemplate.update(sql, request.getIpAddress(), request.getInputText(), request.getTranslatedText());
    }
}