package com.waters.punchout.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PunchOutResponse {
    private boolean success;
    private String sessionKey;
    private String catalogUrl;
    private String message;
}
