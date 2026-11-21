package com.africanheritagehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AfricanHeritageHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(AfricanHeritageHubApplication.class, args);
    }
}
