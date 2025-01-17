package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class FireHandler implements HttpHandler {
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

