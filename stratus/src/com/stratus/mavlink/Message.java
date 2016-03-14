package com.stratus.mavlink;

public abstract class Message {
	
    public abstract int getID();
    public abstract int getPayload();
    public abstract byte getCRC();
    public abstract void Unpack(byte[] payload, MAVlink mavlink);
    
    public DataHelper dh = new DataHelper();   
}
