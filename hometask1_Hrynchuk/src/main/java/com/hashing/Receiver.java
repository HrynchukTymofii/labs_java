package com.hashing;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class Receiver {
    private static byte[] decrypt(byte[] data, SecretKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private static Packet parsePacket(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        Packet packet = new Packet();
        packet.setBMagic(buffer.get());
        packet.setBSrc(buffer.get());
        packet.setBPktId(buffer.getLong());
        packet.setWLen(buffer.getInt());
        packet.setHeadCrc16(buffer.getShort());
        byte[] headerData = Arrays.copyOfRange(data, 0, 14);
        short receivedHeaderCrc = CRC16.calcCRC16(headerData);

        if (receivedHeaderCrc != packet.getHeadCrc16()) {
            throw new IllegalArgumentException("Invalid header CRC16");
        }

        byte[] message = new byte[packet.getWLen()];
        buffer.get(message);
        packet.setBMsq(message);
        packet.setMessageCrc16(buffer.getShort());

        if (CRC16.calcCRC16(message) != packet.getMessageCrc16()) {
            throw new IllegalArgumentException("Invalid message CRC16");
        }

        return packet;
    }

    private static Message parseMessage(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        Message message = new Message();
        message.setCType(buffer.getInt());
        message.setBUserId(buffer.getInt());

        byte[] text = new byte[data.length - 8];
        buffer.get(text);
        message.setMessage(text);

        return message;
    }

    public static Message receivePacket(byte[] data, SecretKey key) throws GeneralSecurityException {
        Packet parsedPacket = parsePacket(data);
        byte[] decryptedMessage = decrypt(parsedPacket.getBMsq(), key);
        Message parsedMessage = parseMessage(decryptedMessage);
        return parsedMessage;
    }
}
