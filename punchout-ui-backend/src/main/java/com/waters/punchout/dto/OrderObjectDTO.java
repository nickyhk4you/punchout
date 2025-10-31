package com.waters.punchout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderObjectDTO {
    private String sessionKey;
    private String type;
    private String operation;
    private String mode;
    private String uniqueName;
    private String userEmail;
    private String companyCode;
    private String userFirstName;
    private String userLastName;
    private String fromIdentity;
    private String soldToLookup;
    private String contactEmail;
}
