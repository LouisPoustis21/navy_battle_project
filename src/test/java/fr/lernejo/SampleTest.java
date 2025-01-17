package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class SampleTest {

    private HttpServer server;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() throws Exception {
        
        server = HttpServer.create(new InetSocketAddress(9876), 0);
        server.createContext("/ping", new PingHandler());
        server.createContext("/api/game/start", new StartHandler(9876, null));
        server.createContext("/api/game/fire", new FireHandler());
        server.start();

        
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
        System.setOut(System.out); 
    }

    @Test
    void testPingHandler() throws Exception {
        var response = HttpClientTestHelper.sendGetRequest("http://localhost:9876/ping");
        assertThat(response).isEqualTo("OK");
    }

    @Test
    void testStartHandler_PostRequest_Success() throws Exception {
        String requestBody = "{\"id\": \"test-id\", \"url\": \"http://localhost:9876\", \"message\": \"Hello\"}";
        var response = HttpClientTestHelper.sendPostRequest("http://localhost:9876/api/game/start", requestBody);
        assertThat(response).contains("May the best code win");
    }

    @Test
    void testStartHandler_PostRequest_Error() throws Exception {
        String invalidRequestBody = "invalid-json";
        var response = HttpClientTestHelper.sendPostRequest("http://localhost:9876/api/game/start", invalidRequestBody);
        assertThat(response).isEmpty();
    }

    @Test
    void testFireHandler_GetRequest_Success() throws Exception {
        var response = HttpClientTestHelper.sendGetRequest("http://localhost:9876/api/game/fire?cell=A1");
        assertThat(response).contains("\"consequence\": \"miss\"");
    }

    @Test
    void testFireHandler_GetRequest_MissingCell() throws Exception {
        var response = HttpClientTestHelper.sendGetRequest("http://localhost:9876/api/game/fire");
        assertThat(response).isEmpty();
    }

    @Test
    void testLauncher_Main_PortMissing() {
        String[] args = {};
        Launcher.main(args);
        assertThat(outputStream.toString()).contains("Usage: java Launcher <port> [<adversary_url>]");
    }

    @Test
    void testLauncher_Main_InvalidPort() {
        String[] args = {"invalid_port"};
        Launcher.main(args);
        assertThat(outputStream.toString()).contains("Invalid port number");
    }

    @Test
    void testLauncher_Main_ValidPort() {
        String[] args = {"9877"};
        Launcher.main(args);
        assertThat(outputStream.toString()).contains("Server started on port 9877");
    }
}

