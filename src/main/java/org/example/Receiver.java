package org.example;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Receiver {
    public static void receiveMessage() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int i = 0;
        int cType, amount;
        while (i < 10) {
            /* cType = 1  command = Add some amount of product
             * cType = 2  command = Delete some amount of product */
            cType = (int) (Math.random() * 2) + 1;
            amount = (int) (Math.random() * 50) + 1;
            System.out.println("Received message: cType = " + cType + ", amount = " + amount);

            Message message = new Message();
            message.setCType(cType);
            message.setAmount(amount);

            byte[] encryptedMessage = Encriptor.encrypt(message);
            Decriptor.decrypt(encryptedMessage);

            i++;
        }
    }
}
