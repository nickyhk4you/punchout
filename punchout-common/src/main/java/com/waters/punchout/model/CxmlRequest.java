package com.waters.punchout.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CxmlRequest {
    private String customerId;
    private String cxmlContent;
    private String documentType;
}
