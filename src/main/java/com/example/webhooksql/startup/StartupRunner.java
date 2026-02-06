package com.example.webhooksql.startup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.webhooksql.service.WebhookService;

@Component
public class StartupRunner implements CommandLineRunner {

    private final WebhookService webhookService;

    public StartupRunner(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Application started...");

        webhookService.startProcess();
    }
}
