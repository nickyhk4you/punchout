package com.waters.punchout.gateway.dto;

public record PunchOutResponse(
    boolean success,
    String sessionKey,
    String catalogUrl,
    String message
) {
}
