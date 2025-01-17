package fr.lernejo.navy_battle;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class LauncherTest {

    private static final int TEST_PORT = 9876;
    private static Launcher launcher;

    @BeforeAll
    static void setUp() {
        new Thread(() -> Launcher.main(new String[]{String.valueOf(TEST_PORT)})).start();
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        
    }

    @Test
    void testPingHandler() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/ping").openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());

        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            assertTrue(scanner.hasNext());
            assertEquals("OK", scanner.nextLine());
        }
    }

    @Test
    void testStartHandler_validRequest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/game/start").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        String requestBody = "{\"id\":\"test-id\",\"url\":\"http://localhost:8795\",\"message\":\"Test message\"}";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes());
        }

        assertEquals(202, connection.getResponseCode());

        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            assertTrue(scanner.hasNext());
            String response = scanner.nextLine();
            assertTrue(response.contains("id"));
            assertTrue(response.contains("url"));
            assertTrue(response.contains("message"));
        }
    }

    @Test
    void testStartHandler_invalidRequest_missingField() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/game/start").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        
        String requestBody = "{\"id\":\"test-id\",\"url\":\"http://localhost:8795\"}"; 
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes());
        }

        assertEquals(400, connection.getResponseCode()); 
    }

    @Test
    void testFireHandler_validRequest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/game/fire?cell=A1").openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());

        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            assertTrue(scanner.hasNext());
            String response = scanner.nextLine();
            assertTrue(response.contains("consequence"));
            assertTrue(response.contains("shipLeft"));
        }
    }

    @Test
    void testFireHandler_invalidRequest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/game/fire").openConnection();
        connection.setRequestMethod("GET");

        assertEquals(400, connection.getResponseCode());
    }

    @Test
    void testFireHandler_invalidCellRequest() throws IOException {
        
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/game/fire?cell=Z9").openConnection();
        connection.setRequestMethod("GET");

        assertEquals(400, connection.getResponseCode());  
    }

    @Test
    void testStartHandler_invalidJsonFormat() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + TEST_PORT + "/api/game/start").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        
        String requestBody = "{\"id\":\"test-id\",,}";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes());
        }

        assertEquals(400, connection.getResponseCode());  
    }

    @Test
    void testPingHandler_serverNotStarted() throws IOException {
      
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + (TEST_PORT + 1) + "/ping").openConnection();
        connection.setRequestMethod("GET");

        assertEquals(404, connection.getResponseCode());  
    }
}

