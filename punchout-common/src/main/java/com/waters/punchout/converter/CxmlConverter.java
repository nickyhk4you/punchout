package com.waters.punchout.converter;

public interface CxmlConverter {
    Object convert(String cxmlContent, String documentType);
    boolean supports(String customerId);
    String getCustomerId();
}
