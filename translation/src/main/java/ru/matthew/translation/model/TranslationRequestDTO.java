package ru.matthew.translation.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TranslationRequestDTO {
    private String sourceLanguageCode;
    private String targetLanguageCode;
    private String texts;
}
