package com.waters.punchout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogRouteDTO {
    
    private String id;
    private String routeName;
    private String domain;
    private String network;
    private String type;
    private String description;
    private List<EnvironmentConfigDTO> environments;
    private Boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvironmentConfigDTO {
        private String environment;
        private String url;
        private String username;
        private String password;
        private String sharedSecret;
        private Boolean enabled;
        private String notes;
    }
}
