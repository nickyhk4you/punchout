package com.waters.punchout.common.dto;

public record PunchOutResponse(
    boolean success,
    String sessionKey,
    String catalogUrl,
    String message
) {
}
