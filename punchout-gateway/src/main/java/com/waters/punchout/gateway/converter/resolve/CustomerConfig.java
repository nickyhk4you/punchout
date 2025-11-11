package com.waters.punchout.gateway.converter.resolve;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerConfig {
    private String id;
    private String version;
    private String dialect;
    private MatchCriteria match;
    private Map<String, String> extrinsicToField;
    private Boolean forceNewSessionKey;
    private List<String> requiredExtrinsics;
    private List<String> allowedCredentialDomains;
}
