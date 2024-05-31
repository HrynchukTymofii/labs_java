package org.example;

public class Message {
    private int cType;
    private int amount;

    public void setCType(int cType){
        this.cType = cType;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }

    public int getCType(){ return this.cType; }
    public int getAmount(){ return this.amount; }
}
