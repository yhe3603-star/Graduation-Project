package com.dongmedicine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class StartupInfoPrinter implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            String baseUrl = "http://localhost:" + serverPort;
            
            System.out.println();
            System.out.println();
            System.out.println("\u001B[32m╔══════════════════════════════════════════════════════════════════════╗\u001B[0m");
            System.out.println("\u001B[32m║                                                                      ║\u001B[0m");
            System.out.println("\u001B[32m║          Dong Medicine Platform Started Successfully!                ║\u001B[0m");
            System.out.println("\u001B[32m║                                                                      ║\u001B[0m");
            System.out.println("\u001B[32m╠══════════════════════════════════════════════════════════════════════╣\u001B[0m");
            System.out.println("\u001B[32m║  Profile:  \u001B[36m" + padRight(activeProfile, 49) + "\u001B[32m║\u001B[0m");
            System.out.println("\u001B[32m╠══════════════════════════════════════════════════════════════════════╣\u001B[0m");
            System.out.println("\u001B[32m║  API:      \u001B[36m" + padRight(baseUrl + "/api/", 49) + "\u001B[32m║\u001B[0m");
            System.out.println("\u001B[32m║  Swagger:  \u001B[36m" + padRight(baseUrl + "/swagger-ui/index.html", 49) + "\u001B[32m║\u001B[0m");
            System.out.println("\u001B[32m╚══════════════════════════════════════════════════════════════════════╝\u001B[0m");
            System.out.println();
        }, 500, TimeUnit.MILLISECONDS);
    }
    
    private String padRight(String s, int length) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
