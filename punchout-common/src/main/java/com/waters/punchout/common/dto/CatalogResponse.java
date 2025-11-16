package com.waters.punchout.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogResponse(
    String catalogUrl,
    String sessionKey,
    String status,
    String message
) {
}
