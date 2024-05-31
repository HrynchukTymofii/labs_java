package org.example;

public class Main {
    public static void main(String[] args) {
        try {
            Receiver.receiveMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}