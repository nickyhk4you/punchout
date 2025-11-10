package com.waters.punchout.gateway.converter.config;

import com.waters.punchout.gateway.converter.resolve.CustomerConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "punchout.conversion")
@Data
public class PunchoutConversionProperties {
    
    private List<CustomerConfig> customers = new ArrayList<>();
}
