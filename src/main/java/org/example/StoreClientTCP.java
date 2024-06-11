package org.example;

import java.io.*;
import java.net.*;

public class StoreClientTCP {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String ip;
    private int port;

    public void startConnection(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        connect();
    }

    private void connect() throws IOException {
        while (true) {
            try {
                clientSocket = new Socket(ip, port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                break;
            } catch (IOException e) {
                System.out.println("Unable to connect to server, retrying...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public String sendMessage(String msg) throws IOException {
        if (clientSocket.isClosed() || !clientSocket.isConnected()) {
            connect();
        }
        out.println(msg);
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
