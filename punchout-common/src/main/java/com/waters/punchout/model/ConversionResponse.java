package com.waters.punchout.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private boolean success;
    private String message;
    private Object data;
    private String customerId;
    private String documentType;
}
