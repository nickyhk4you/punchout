package com.waters.punchout.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "punchout_session", indexes = {
    @Index(name = "idx_session_date", columnList = "sessionDate"),
    @Index(name = "idx_contact_email", columnList = "contactEmail"),
    @Index(name = "idx_environment", columnList = "environment")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunchOutSession {
    
    @Id
    @NotBlank(message = "Session key is mandatory")
    private String sessionKey;
    
    private String cartReturn;
    
    @NotBlank(message = "Operation is mandatory")
    private String operation;
    
    private String contactEmail;
    
    private String routeName;
    
    private String environment;
    
    private String flags;
    
    @NotNull(message = "Session date is mandatory")
    private LocalDateTime sessionDate;
    
    private LocalDateTime punchedIn;
    
    private LocalDateTime punchedOut;
    
    private String orderId;
    
    private BigDecimal orderValue;
    
    private Integer lineItems;
    
    private Integer itemQuantity;
    
    private String catalog;
    
    private String network;
    
    private String parser;
    
    private String buyerCookie;
}
