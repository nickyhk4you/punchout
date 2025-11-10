package com.waters.punchout.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "thirdparty")
@Data
public class ThirdPartyProperties {
    
    private AuthConfig auth = new AuthConfig();
    private MuleConfig mule = new MuleConfig();
    
    @Data
    public static class AuthConfig {
        private String url = "http://localhost:8082/api/v1/token";
    }
    
    @Data
    public static class MuleConfig {
        private String url = "http://localhost:8082/api/v1/catalog";
    }
}
