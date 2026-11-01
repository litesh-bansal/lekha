package com.lekha.langchain;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Lekha {
    public static void main(String[] args) {
        SpringApplication.run(Lekha.class, args);
    }
}

