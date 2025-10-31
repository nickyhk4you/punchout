package com.waters.punchout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRequestDTO {
    private Long id;
    private String sessionKey;
    private LocalDateTime datetime;
    private String uri;
    private String openLink;
}
