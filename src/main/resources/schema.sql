CREATE TABLE translation_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(255),
    input_text TEXT,
    translated_text TEXT
);