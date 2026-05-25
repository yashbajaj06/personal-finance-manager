package com.finance.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Personal Finance Manager application.
 * Provides REST APIs for managing personal finances including
 * transactions, categories, savings goals, and reports.
 */
@SpringBootApplication
public class PersonalFinanceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceManagerApplication.class, args);
    }
}
