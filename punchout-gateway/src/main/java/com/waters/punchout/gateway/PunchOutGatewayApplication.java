package com.waters.punchout.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class PunchOutGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PunchOutGatewayApplication.class, args);
    }
}
