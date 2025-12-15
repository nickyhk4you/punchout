package com.waters.punchout.config;

import com.waters.punchout.repository.GatewayRequestRepository;
import com.waters.punchout.repository.OrderObjectRepository;
import com.waters.punchout.repository.PunchOutSessionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("local")
@EnableJpaRepositories(basePackages = "com.waters.punchout.repository")
public class JpaConfiguration {

    @Bean
    public DataInitializer dataInitializer(PunchOutSessionRepository sessionRepository,
                                           OrderObjectRepository orderObjectRepository,
                                           GatewayRequestRepository gatewayRequestRepository) {
        return new DataInitializer(sessionRepository, orderObjectRepository, gatewayRequestRepository);
    }
}
