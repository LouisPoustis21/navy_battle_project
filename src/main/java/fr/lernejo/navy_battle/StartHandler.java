package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class StartHandler implements HttpHandler {
    private final int port;
    private final String adversaryUrl;

    public StartHandler(int port, String adversaryUrl) {
        this.port = port;
        this.adversaryUrl = adversaryUrl;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("/api/game/start received: " + requestBody);

            try {
                String response = String.format(
                    "{\"id\": \"%s\", \"url\": \"http://localhost:%d\", \"message\": \"May the best code win\"}",
                    UUID.randomUUID().toString(), port
                );
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 202, response);

                if (adversaryUrl != null) {
                    sendFireRequest(adversaryUrl, "A1");
                }

            } catch (Exception e) {
                exchange.sendResponseHeaders(400, -1);
            }

        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    public static void sendStartRequest(int port, String adversaryUrl) {
        HttpClient client = HttpClient.newHttpClient();
        String id = UUID.randomUUID().toString();
        String body = String.format(
            "{\"id\": \"%s\", \"url\": \"http://localhost:%d\", \"message\": \"Hello from %d\"}",
            id, port, port
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adversaryUrl + "/api/game/start"))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> System.out.println("Response from adversary: " + response.body()))
            .exceptionally(e -> {
                System.err.println("Failed to contact adversary: " + e.getMessage());
                return null;
            });
    }

    private void sendFireRequest(String adversaryUrl, String cell) {
        HttpClient client = HttpClient.newHttpClient();
        String fireUrl = adversaryUrl + "/api/game/fire?cell=" + cell;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fireUrl))
            .header("Accept", "application/json")
            .GET()
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> System.out.println("Fire response: " + response.body()))
            .exceptionally(e -> {
                System.err.println("Failed to send fire request: " + e.getMessage());
                return null;
            });
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

