package org.example;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Decriptor {
    private static final String KEY = "1234567890123456";

    public static void decrypt(byte[] encryptedMessage) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKey key = new SecretKeySpec(KEY.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);

        ByteBuffer buffer = ByteBuffer.wrap(decryptedBytes);
        buffer.order(ByteOrder.BIG_ENDIAN);

        int cType = buffer.getInt();
        int amount = buffer.getInt();

        Message decryptedMessage = new Message();
        decryptedMessage.setCType(cType);
        decryptedMessage.setAmount(amount);

        Processor.process(decryptedMessage);

    }
}