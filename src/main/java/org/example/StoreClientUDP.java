package org.example;

import java.io.IOException;
import java.net.*;

public class StoreClientUDP {
    DatagramSocket socket;
    InetAddress address;
    private byte[] buf;
    private static final int TIMEOUT = 1000;
    private static final int MAX_RETRIES = 5;

    public StoreClientUDP() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT);
        address = InetAddress.getByName("localhost");
    }

    public String sendPacket(String msg) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);

        byte[] responseBuf = new byte[256];
        DatagramPacket responsePacket = new DatagramPacket(responseBuf, responseBuf.length);

        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                socket.receive(responsePacket);
                return new String(responsePacket.getData(), 0, responsePacket.getLength());
            } catch (SocketTimeoutException e) {
                retries++;
                System.out.println("Timeout, retrying... (" + retries + "/" + MAX_RETRIES + ")");
                socket.send(packet);
            }
        }
        throw new IOException("No response from server after " + MAX_RETRIES + " retries");
    }

    public void close() {
        socket.close();
    }
}
