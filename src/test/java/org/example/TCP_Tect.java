package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class TCP_Tect {
    private StoreServerTCP server;
    private ExecutorService executorService;

    @BeforeEach
    public void setUp() throws IOException {
        server = new StoreServerTCP();
        executorService = Executors.newFixedThreadPool(5);
        new Thread(() -> {
            try {
                server.start(4445);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.stop();
        executorService.shutdown();
    }

    @Test
    public void testMultipleClients() throws Exception {
        Future<String>[] futures = new Future[5];

        for (int i = 0; i < 5; i++) {
            futures[i] = executorService.submit(() -> {
                try {
                    StoreClientTCP client = new StoreClientTCP();
                    client.startConnection("localhost", 4445);
                    String response = client.sendMessage("Hello Server");
                    client.stopConnection();
                    return response;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        }

        for (Future<String> future : futures) {
            assertEquals("Hello Server", future.get());
        }
    }

    @Test
    public void testClientReconnection() throws Exception {
        StoreClientTCP client = new StoreClientTCP();
        client.startConnection("localhost", 4445);

        server.stop();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                server.start(4445);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        String response = client.sendMessage("Hello again");
        assertEquals("Hello again", response);

        client.stopConnection();
    }

}
