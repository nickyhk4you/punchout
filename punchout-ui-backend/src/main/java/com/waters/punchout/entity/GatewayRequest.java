package com.waters.punchout.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_request", indexes = {
    @Index(name = "idx_gateway_session_key", columnList = "sessionKey"),
    @Index(name = "idx_gateway_datetime", columnList = "datetime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Session key is mandatory")
    private String sessionKey;
    
    @NotNull(message = "Datetime is mandatory")
    private LocalDateTime datetime;
    
    @NotBlank(message = "URI is mandatory")
    private String uri;
    
    private String openLink;
}
