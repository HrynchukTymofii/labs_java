package com.hashing;

public class Message {
    private int cType;
    private int bUserId;
    private byte[] message;

    public void setCType(int cType){
        this.cType = cType;
    }

    public void setBUserId(int bUserId){
        this.bUserId = bUserId;
    }

    public void setMessage(byte[] message){
        this.message = message;
    }

    public int getCType(){ return this.cType; }
    public int getBUserId(){ return this.bUserId; }
    public byte[] getMessage(){ return this.message; }

}
