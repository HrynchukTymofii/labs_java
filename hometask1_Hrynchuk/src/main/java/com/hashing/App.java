package com.hashing;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

public class App
{
    public static SecretKey generateKey() throws GeneralSecurityException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    public static void main( String[] args ) throws GeneralSecurityException {
        SecretKey key = generateKey();
        byte[] sendedMessage = Sender.sendPacket(1, 444, "This is a secret!", 2, 124, key);
        for(int i=0; i< sendedMessage.length ; i++) {
            System.out.print(sendedMessage[i] +" ");
        }
        System.out.println();
        Message message = Receiver.receivePacket(sendedMessage, key);
        System.out.println(new String(message.getMessage()));
    }
}
