package com.waters.punchout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.waters.punchout")
public class PunchoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(PunchoutApplication.class, args);
    }
}
