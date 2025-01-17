package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
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
                StartHandler.sendStartRequest(port, adversaryUrl);
            }

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

