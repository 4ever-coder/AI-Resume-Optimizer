package com.qianyi.resumeoptimizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ResumeOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeOptimizerApplication.class, args);
    }
}
