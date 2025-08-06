package com.webscraper.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class WebScraperApplication {
    public static void main(String[] args) {
        // Set system property for headless mode to false for Swing
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(WebScraperApplication.class, args);
    }
}

