package com.hashing;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;

public class Sender {
    private static byte[] encrypt(byte[] data, SecretKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private static byte[] serializeMessage(Message message) {
        ByteBuffer buffer = ByteBuffer.allocate(8 + message.getMessage().length);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putInt(message.getCType());
        buffer.putInt(message.getBUserId());
        buffer.put(message.getMessage());

        return buffer.array();
    }

    private static byte[] serializePacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(16 + packet.getWLen() + 2);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(packet.getBMagic());
        buffer.put(packet.getBSrc());
        buffer.putLong(packet.getBPktId());
        buffer.putInt(packet.getWLen());
        buffer.putShort((short) packet.getHeadCrc16());
        buffer.put(packet.getBMsq());
        buffer.putShort((short) packet.getMessageCrc16());

        return buffer.array();
    }

    private static byte[] serializePacketHead(Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(14);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(packet.getBMagic());
        buffer.put(packet.getBSrc());
        buffer.putLong(packet.getBPktId());
        buffer.putInt(packet.getWLen());

        return buffer.array();
    }

    public static byte[] sendPacket(int cType, int bUserId, String text, int bSrc, long bPktId, SecretKey key ) throws GeneralSecurityException {
        Message message = new Message();
        message.setCType(cType);
        message.setBUserId(bUserId);
        message.setMessage(text.getBytes());

        byte[] encryptedMessage = encrypt(serializeMessage(message), key);

        Packet packet = new Packet();
        packet.setBMagic((byte) 0x13);
        packet.setBSrc((byte) bSrc);
        packet.setBPktId(bPktId);
        packet.setWLen(encryptedMessage.length);
        packet.setHeadCrc16(CRC16.calcCRC16(serializePacketHead(packet)));
        packet.setBMsq(encryptedMessage);
        packet.setMessageCrc16(CRC16.calcCRC16(encryptedMessage));

        return serializePacket(packet);
    }


}
