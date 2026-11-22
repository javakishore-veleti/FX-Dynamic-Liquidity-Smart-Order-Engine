package com.jk.apps.banking.fx.sor.quote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QuoteServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(QuoteServiceApp.class, args);
    }
}
