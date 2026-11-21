package com.gps.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GpsCoreServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GpsCoreServerApplication.class, args);
    }
}
