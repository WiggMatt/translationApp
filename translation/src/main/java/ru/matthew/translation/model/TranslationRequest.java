package ru.matthew.translation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationRequest {
    private Long id;
    private String ipAddress;
    private String inputText;
    private String translatedText;
}
