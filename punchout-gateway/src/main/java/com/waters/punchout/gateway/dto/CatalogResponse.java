package com.waters.punchout.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogResponse(
    String catalogUrl,
    String sessionKey,
    String status,
    String message
) {
}
