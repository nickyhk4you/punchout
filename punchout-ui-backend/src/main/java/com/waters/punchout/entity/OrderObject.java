package com.waters.punchout.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_object")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderObject {
    
    @Id
    @NotBlank(message = "Session key is mandatory")
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
