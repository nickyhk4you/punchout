package com.waters.punchout.gateway.converter.resolve;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCriteria {
    private String fromDomain;
    private String fromIdentity;
    private String fromIdentityPattern;
    private String toIdentity;
    private String toIdentityPattern;
    private String senderUserAgentPattern;
}
