package ukma.edu;

import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

public class HttpServerTest {

    private static final int PORT = 8765;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static HttpServer httpServer;

    @BeforeAll
    public static void setUp() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/login", new ukma.edu.HttpServer.LoginHandler());
        httpServer.createContext("/api/good", new ukma.edu.HttpServer.GoodHandler());
        httpServer.createContext("/api/good/", new ukma.edu.HttpServer.SpecificGoodHandler());
        httpServer.start();
        System.out.println("HTTP Server started on port " + PORT);
    }

    @AfterAll
    public static void tearDown() {
        httpServer.stop(0);
        System.out.println("HTTP Server stopped.");
    }

    @Test
    public void testLogin_Successful() throws IOException {
        URL url = new URL(BASE_URL + "/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        con.setDoOutput(true);

        JSONObject requestBody = new JSONObject()
                .put("login", "user1")
                .put("password", "password1");

        con.getOutputStream().write(requestBody.toString().getBytes());

        int responseCode = con.getResponseCode();

        Assertions.assertEquals(HttpStatus.SC_OK, responseCode);

        InputStream inputStream = con.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder responseBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBody.append(line);
        }
        reader.close();
        System.out.println("Response body: " + responseBody.toString());

        con.disconnect();
    }

    @Test
    public void testPostGood_Successful() throws IOException {
        URL url = new URL(BASE_URL + "/api/good");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        con.setDoOutput(true);

        JSONObject requestBody = new JSONObject()
                .put("name", "New Good")
                .put("price", 10.0);

        con.getOutputStream().write(requestBody.toString().getBytes());

        int responseCode = con.getResponseCode();

        Assertions.assertEquals(HttpStatus.SC_CREATED, responseCode);

        InputStream inputStream = con.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder responseBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBody.append(line);
        }
        reader.close();
        System.out.println("POST Good Response body: " + responseBody.toString());

        con.disconnect();
    }
}
