package org.example;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UDP_Test {

    private StoreServerUDP server;

    @Before
    public void setUp() throws SocketException {
        server = new StoreServerUDP();
        server.start();
    }

    @After
    public void tearDown() {
        server.interrupt();
    }

    @Test
    public void testMultipleClients() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Future[] futures = new Future[5];

        for (int i = 0; i < 5; i++) {
            futures[i] = executorService.submit(() -> {
                try {
                    StoreClientUDP client = new StoreClientUDP();
                    String response = client.sendPacket("Hello Server");
                    client.close();
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

        executorService.shutdown();
    }

    @Test
    public void testClientRetries() throws IOException {
        DatagramSocket mockSocket = new DatagramSocket() {
            private int receiveCount = 0;

            @Override
            public synchronized void receive(java.net.DatagramPacket p) throws IOException {
                if (receiveCount++ < 2) {
                    throw new SocketTimeoutException("Mock timeout");
                }
                super.receive(p);
            }
        };

        InetAddress address = InetAddress.getByName("localhost");
        StoreClientUDP client = new StoreClientUDP();
        client.socket = mockSocket;  // Use mock socket to simulate timeout
        client.address = address;

        String response = client.sendPacket("Hello Server");
        assertEquals("Hello Server", response);
    }

    @Test
    public void testClientMaxRetriesExceeded() throws SocketException, UnknownHostException {
        DatagramSocket mockSocket = new DatagramSocket() {
            @Override
            public synchronized void receive(java.net.DatagramPacket p) throws IOException {
                throw new SocketTimeoutException("Mock timeout");
            }
        };

        InetAddress address = InetAddress.getByName("localhost");
        StoreClientUDP client = new StoreClientUDP();
        client.socket = mockSocket;
        client.address = address;

        assertThrows(IOException.class, () -> client.sendPacket("Hello Server"));
    }
}
