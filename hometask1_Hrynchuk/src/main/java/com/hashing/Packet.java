package com.hashing;

public class Packet {
    private byte bMagic;
    private byte bSrc;
    private long bPktId;
    private int wLen;
    private short headCrc16;
    private byte[] bMsq;
    private short messageCrc16;

    public void setBMagic(byte bMagic) {
        this.bMagic = bMagic;
    }
    public void setBSrc(byte bSrc) {
        this.bSrc = bSrc;
    }
    public void setBPktId(long bPktId) {
        this.bPktId = bPktId;
    }
    public void setWLen(int wLen) {
        this.wLen = wLen;
    }
    public void setHeadCrc16(short headCrc16) {
        this.headCrc16 = headCrc16;
    }
    public void setBMsq(byte[] bMsq) {
        this.bMsq = bMsq;
    }
    public void setMessageCrc16(short messageCrc16) {
        this.messageCrc16 = messageCrc16;
    }

    public byte getBMagic() {return this.bMagic;}
    public byte getBSrc() {return this.bSrc;}
    public long getBPktId() {return this.bPktId;}
    public int getWLen() {return this.wLen;}
    public short getHeadCrc16() {return this.headCrc16;}
    public byte[] getBMsq() {return this.bMsq;}
    public short getMessageCrc16() {return this.messageCrc16;}
}
