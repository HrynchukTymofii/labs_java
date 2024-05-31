package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Processor {
    private static int totalAmount = 100;

    public static void process(Message message) {
        int cType = message.getCType();
        int amount = message.getAmount();

        synchronized (Processor.class) {
            switch (cType) {
                case 1:
                    totalAmount += amount;
                    System.out.println("Added " + amount + ". Total amount: " + totalAmount);
                    break;
                case 2:
                    if (totalAmount >= amount) {
                        totalAmount -= amount;
                        System.out.println("Removed " + amount + ". Total amount: " + totalAmount);
                    } else {
                        System.out.println("Cannot remove " + amount + ". Not enough stock. Total amount: " + totalAmount);
                    }
                    break;
                default:
                    System.out.println("Unknown command type: " + cType);
            }
        }

        byte[] mess = new byte[10];
        Sender.sendMessage(mess);
    }
}
