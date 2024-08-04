package ru.matthew.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenResponse {
    @JsonProperty("iamToken")
    private String iamToken;
    @JsonProperty("expiresAt")
    private String expiresAt;
}
