package fr.lernejo.navy_battle.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtils {
    public static void sendFireRequest(String adversaryUrl, String cell) {
        HttpClient client = HttpClient.newHttpClient();
        String fireUrl = adversaryUrl + "/api/game/fire?cell=" + cell;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fireUrl))
            .header("Accept", "application/json")
            .GET()
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                System.out.println("Fire response: " + response.body());
            })
            .exceptionally(e -> {
                System.err.println("Failed to send fire request: " + e.getMessage());
                return null;
            });
    }
}

