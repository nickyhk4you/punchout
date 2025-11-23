package com.waters.punchout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.waters.punchout")
@EnableJpaRepositories(basePackages = "com.waters.punchout.repository")
@EnableMongoRepositories(basePackages = "com.waters.punchout.mongo.repository")
public class PunchoutUiBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PunchoutUiBackendApplication.class, args);
    }
}
