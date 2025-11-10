package com.waters.punchout.gateway.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderItem {
    private Integer lineNumber;
    private Integer quantity;
    private String supplierPartId;
    private String supplierPartAuxiliaryId;
    private String description;
    private BigDecimal unitPrice;
    private BigDecimal extendedAmount;
    private String currency;
    private String unitOfMeasure;
    private String unspsc;
    private String url;
    private Map<String, String> extrinsics;
}
