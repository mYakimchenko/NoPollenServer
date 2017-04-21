package com.mihanjk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PollenServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PollenServerApplication.class, args);
    }
}
