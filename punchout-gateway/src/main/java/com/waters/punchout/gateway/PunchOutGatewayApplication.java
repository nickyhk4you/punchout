package com.waters.punchout.gateway;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
@EnableEncryptableProperties
public class PunchOutGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PunchOutGatewayApplication.class, args);
    }
}
