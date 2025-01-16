package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Launcher <port> [<adversary_url>]");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[0]);
            System.exit(1);
            return; 
        }

        String adversaryUrl = args.length > 1 ? args[1] : null;

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/ping", new PingHandler());
            server.createContext("/api/game/start", new StartHandler(port, adversaryUrl));
            server.createContext("/api/game/fire", new FireHandler());

            ExecutorService executor = Executors.newFixedThreadPool(1);
            server.setExecutor(executor);

            server.start();
            System.out.println("Server started on port " + port);

            if (adversaryUrl != null) {
                sendStartRequest(port, adversaryUrl);
            }

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendStartRequest(int port, String adversaryUrl) {
        HttpClient client = HttpClient.newHttpClient();
        String id = UUID.randomUUID().toString();
        String body = String.format("{\"id\": \"%s\", \"url\": \"http://localhost:%d\", \"message\": \"Hello from %d\"}", id, port, port);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adversaryUrl + "/api/game/start"))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                System.out.println("Response from adversary: " + response.body());
            })
            .exceptionally(e -> {
                System.err.println("Failed to contact adversary: " + e.getMessage());
                return null;
            });
    }

    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "OK";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class StartHandler implements HttpHandler {
        private final int port;
        private final String adversaryUrl;

        StartHandler(int port, String adversaryUrl) {
            this.port = port;
            this.adversaryUrl = adversaryUrl;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                System.out.println("/api/game/start received: " + requestBody);

                try {
                    String response = String.format("{\"id\": \"%s\", \"url\": \"http://localhost:%d\", \"message\": \"May the best code win\"}", UUID.randomUUID().toString(), port);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(202, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
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

        private void sendFireRequest(String adversaryUrl, String cell) {
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

    static class FireHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String cell = query != null && query.startsWith("cell=") ? query.substring(5) : null;

                if (cell == null || cell.isEmpty()) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                System.out.println("/api/game/fire received: cell=" + cell);

                String response = "{\"consequence\": \"miss\", \"shipLeft\": true}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}

