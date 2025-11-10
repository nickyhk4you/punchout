package com.waters.punchout.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunchOutRequest {
    private String sessionKey;
    private String operation; // create, edit, inspect
    private String buyerCookie;
    private String contactEmail;
    private String cartReturnUrl;
    private String fromIdentity;
    private String toIdentity;
    private String senderIdentity;
    private Map<String, String> additionalFields;
    private LocalDateTime timestamp;
}
