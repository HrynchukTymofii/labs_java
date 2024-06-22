package ukma.edu;

import java.io.*;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;

public class HttpServer {
    private static final Map<String, String> userCredentials = new HashMap<>();
    private static final Map<String, String> goods = new HashMap<>();
    private static final Map<String, String> loggedInUsers = new HashMap<>();
    static {
        userCredentials.put("user1", "password1");
        userCredentials.put("user2", "password2");
    }

    public static void main(String[] args) throws Exception {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(8765), 0);
        HttpContext loginContext = server.createContext("/login", new LoginHandler());
        HttpContext goodContext = server.createContext("/api/good", new GoodHandler());
        HttpContext specificGoodContext = server.createContext("/api/good/", new SpecificGoodHandler());

        goodContext.setAuthenticator(new Auth());
        specificGoodContext.setAuthenticator(new Auth());

        server.setExecutor(null);
        server.start();
        System.out.println("Server is listening on port 8765");
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                handlePostRequest(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().reduce("", (accumulator, actual) -> accumulator + actual);

            JSONObject jsonObject = new JSONObject(requestBody);
            String login = jsonObject.getString("login");
            String password = jsonObject.getString("password");

            // Checking if credentials are valid
            if (isValidCredentials(login, password)) {
                String token = generateToken(login);
                loggedInUsers.put(token, login);
                //System.out.println("Logged in user: " + loggedInUsers);

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("token", token);

                String response = jsonResponse.toString();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(401, -1);
            }
        }

        private boolean isValidCredentials(String login, String password) {
            return userCredentials.containsKey(login) && userCredentials.get(login).equals(password);
        }

        private String generateToken(String login) {
            return "token_" + login.hashCode();
        }

    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            jsonBody.append(line).append("\n");
        }
        br.close();
        isr.close();
        return jsonBody.toString();
    }

    public static class GoodHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
                handlePutRequest(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handlePutRequest(HttpExchange exchange) throws IOException {
            try {
                String jsonBody = readRequestBody(exchange);

                JSONObject jsonObject = new JSONObject(jsonBody);

                String name = jsonObject.getString("name");
                double price = jsonObject.getDouble("price");

                if (price < 0) {
                    exchange.sendResponseHeaders(409, -1);
                    return;
                }

                String id = UUID.randomUUID().toString();
                goods.put(id, name);
                //System.out.println(goods);

                String response = "Created good with ID: " + id;
                exchange.sendResponseHeaders(201, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(400, -1);
            }
        }
    }

    static class SpecificGoodHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String[] uriParts = exchange.getRequestURI().toString().split("/");
            String id = uriParts[uriParts.length - 1];

            switch (method.toUpperCase()) {
                case "GET":
                    handleGetRequest(exchange, id);
                    break;
                case "POST":
                    handlePostRequest(exchange, id);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, id);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1);
                    break;
            }
        }

        private void handleGetRequest(HttpExchange exchange, String id) throws IOException {
            if (goods.containsKey(id)) {
                String response = new JSONObject()
                        .put("id", id)
                        .put("name", goods.get(id))
                        .toString();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }

        private void handlePostRequest(HttpExchange exchange, String id) throws IOException {
            try {
                String jsonBody = readRequestBody(exchange);

                JSONObject jsonObject = new JSONObject(jsonBody);

                if (!goods.containsKey(id)) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                String newName = jsonObject.getString("name");
                double newPrice = jsonObject.getDouble("price");

                if (newPrice < 0) {
                    exchange.sendResponseHeaders(409, -1);
                    return;
                }

                goods.put(id, newName);

                exchange.sendResponseHeaders(204, -1);
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(400, -1);
            }
        }

        private void handleDeleteRequest(HttpExchange exchange, String id) throws IOException {
            if (goods.containsKey(id)) {
                goods.remove(id);
                exchange.sendResponseHeaders(204, -1);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    static class Auth extends Authenticator {
        @Override
        public Result authenticate(HttpExchange exchange) {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (loggedInUsers.containsKey(token)) {
                    String login = loggedInUsers.get(token);
                    return new Success(new HttpPrincipal(login, "realm"));
                } else {
                    return new Failure(403);
                }
            } else {
                return new Failure(403);
            }
        }
    }
}
