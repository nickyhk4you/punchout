package com.waters.punchout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PunchOutSessionDTO {
    private String sessionKey;
    private String cartReturn;
    private String operation;
    private String contact;
    private String contactEmail;
    private String routeName;
    private String environment;
    private String flags;
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
