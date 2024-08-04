package ru.matthew.translation.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TranslationRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    public TranslationRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveRequest(String ipAddress) {
        String sql = "INSERT INTO TRANSLATION_REQUESTS (ip_address) VALUES (?)";
        jdbcTemplate.update(sql, ipAddress);
    }
}