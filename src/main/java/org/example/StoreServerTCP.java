package org.example;

import java.io.*;
import java.net.*;

public class StoreServerTCP {
    private ServerSocket serverSocket;
    private boolean running = true;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (running) {
            try {
                new ClientHandler(serverSocket.accept()).start();
            } catch (SocketException e) {
                if (!running) {
                    System.out.println("Server stopped.");
                    break;
                } else {
                    throw e;
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        serverSocket.close();
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        out.println("bye");
                        break;
                    }
                    out.println(inputLine);
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}