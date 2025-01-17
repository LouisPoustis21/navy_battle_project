package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
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
            handlePostRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
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
                sendFireRequest("A1");
            }

        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
        }
    }

    private void sendFireRequest(String cell) {
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

