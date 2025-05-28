package com.loadtester.backend.controller;

import com.loadtester.backend.dto.LoadRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS}) // Allow CORS for POST and OPTIONS only
public class LoadTestController {

    @PostMapping("/send-requests")
    public String sendRequests(@RequestBody LoadRequest request) {
        System.out.println("Received load test request:");
        System.out.println("→ URL: " + request.getUrl());
        System.out.println("→ Number of Requests: " + request.getCount());

        if (request.getCount() <= 0 || request.getUrl() == null || request.getUrl().isBlank()) {
            System.out.println("❌ Invalid request input.");
            return "Invalid input";
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < request.getCount(); i++) {
            int requestId = i + 1;
            executor.submit(() -> {
                try {
                    HttpRequest httpRequest = HttpRequest.newBuilder()
                            .uri(URI.create(request.getUrl()))
                            .GET()
                            .build();
                    client.send(httpRequest, HttpResponse.BodyHandlers.discarding());
                    System.out.println("✅ Request #" + requestId + " sent successfully.");
                } catch (Exception e) {
                    System.out.println("❌ Request #" + requestId + " failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        System.out.println("🚀 All request threads submitted. Executor shutting down.");
        return "Request started";
    }

    // Handles CORS preflight OPTIONS requests
    @RequestMapping(value = "/send-requests", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handlePreflight() {
        return ResponseEntity.ok().build();
    }
}