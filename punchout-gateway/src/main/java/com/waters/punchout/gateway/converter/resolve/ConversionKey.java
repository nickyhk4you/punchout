package com.waters.punchout.gateway.converter.resolve;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversionKey {
    private String customerId;
    private String version;
    private CustomerConfig customerConfig;
    
    public String toKey() {
        return customerId + ":" + version;
    }
}
